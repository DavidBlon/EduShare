package com.wb.learningresourcelibrary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wb.learningresourcelibrary.dto.ParsedResourceVo;
import com.wb.learningresourcelibrary.entity.Category;
import com.wb.learningresourcelibrary.entity.KeywordRule;
import com.wb.learningresourcelibrary.entity.Resource;
import com.wb.learningresourcelibrary.entity.ResourceTag;
import com.wb.learningresourcelibrary.entity.Tag;
import com.wb.learningresourcelibrary.mapper.KeywordRuleMapper;
import com.wb.learningresourcelibrary.mapper.ResourceMapper;
import com.wb.learningresourcelibrary.mapper.ResourceTagMapper;
import com.wb.learningresourcelibrary.mapper.TagMapper;
import com.wb.learningresourcelibrary.service.CategoryService;
import com.wb.learningresourcelibrary.service.ResourceImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 批量导入 Service 实现
 *
 * 关键词规则（标签匹配、教材版本）改为从 keyword_rule 表动态加载，
 * 管理员可在后台管理页面新增/修改规则，无需修改代码。
 * 科目匹配规则从 category 表子分类名称动态构建（排除"试卷"类）。
 */
@Service
@RequiredArgsConstructor
public class ResourceImportServiceImpl implements ResourceImportService {

    private final CategoryService categoryService;
    private final ResourceMapper resourceMapper;
    private final ResourceTagMapper resourceTagMapper;
    private final TagMapper tagMapper;
    private final KeywordRuleMapper keywordRuleMapper;

    // 年级范围：如 "1-6年级" "1-6年" "5、6年级"
    private static final Pattern GRADE_RANGE_PATTERN =
            Pattern.compile("(\\d+)[-、](\\d+)[年年级]?");

    // 独立年级：如 "高一" "高二" "高三" "初一" "初二" "初三" "一年级" ~ "六年级"
    private static final Pattern STANDALONE_GRADE_PATTERN =
            Pattern.compile("(初[一二三]|高[一二三]|[一二三四五六]年级)");

    // 阿拉伯数字年级：如 "7年级" "8年级" "9年级"（须在 Chinese 版没匹配时使用）
    private static final Pattern ARABIC_GRADE_PATTERN =
            Pattern.compile("([1-9])年级");

    // 资源名称：《xxx》
    private static final Pattern BOOK_PATTERN =
            Pattern.compile("《(.+?)》");

    // 链接正则
    private static final Pattern LINK_PATTERN =
            Pattern.compile("(https?://[^\\s]+)");

    // === 以下为动态加载的规则（从数据库读取，不再硬编码） ===

    /** 科目正则（由 loadCategoryData 从 category 表子分类名称动态构建） */
    private Pattern subjectPattern;

    /** 教材版本正则（由 loadKeywordRules 从 keyword_rule 表 VERSION 类型规则构建） */
    private Pattern versionPattern;

    /** 标签关键词映射 关键词→标签名（由 loadKeywordRules 从 keyword_rule 表 TAG 类型规则构建） */
    private Map<String, String> tagKeywordMap;

    /** 缓存所有标签名→ID */
    private Map<String, Long> tagNameIdCache;

    /** 缓存分类数据 */
    private List<Category> allCategories;
    private Map<Long, List<Category>> childrenMap;

    @Override
    public List<ParsedResourceVo> parseText(String text) {
        loadCategoryData();
        loadKeywordRules();

        // 分割文本块（按空行分隔）
        String[] blocks = text.split("\\n\\s*\\n");
        List<ParsedResourceVo> result = new ArrayList<>();

        for (String block : blocks) {
            block = block.trim();
            if (block.isEmpty()) continue;

            ParsedResourceVo vo = parseBlock(block);
            if (vo != null) {
                result.add(vo);
            }
        }

        // 检查是否已存在（根据网盘链接去重）
        markDuplicates(result);

        return result;
    }

    /**
     * 标记已在数据库中存在的资源
     */
    private void markDuplicates(List<ParsedResourceVo> parsedList) {
        // 收集所有有链接的资源
        List<String> links = new ArrayList<>();
        Map<String, ParsedResourceVo> linkIndex = new HashMap<>();
        for (ParsedResourceVo vo : parsedList) {
            if (vo.getNetdiskLink() != null && !vo.getNetdiskLink().isEmpty()) {
                String normalized = vo.getNetdiskLink().trim();
                links.add(normalized);
                linkIndex.put(normalized, vo);
            }
        }
        if (links.isEmpty()) return;

        // 批量查询已存在的链接
        List<Resource> existing = resourceMapper.selectList(
                new LambdaQueryWrapper<Resource>()
                        .in(Resource::getNetdiskLink, links)
                        .select(Resource::getNetdiskLink)
        );

        Set<String> existingLinks = new HashSet<>();
        for (Resource r : existing) {
            if (r.getNetdiskLink() != null) {
                existingLinks.add(r.getNetdiskLink().trim());
            }
        }

        // 标记
        for (String link : existingLinks) {
            ParsedResourceVo vo = linkIndex.get(link);
            if (vo != null) {
                vo.setAlreadyExists(true);
            }
        }
    }

    /**
     * 加载分类数据到缓存，并动态构建科目正则
     *
     * 从 category 表所有子分类中提取名称（排除以"试卷"结尾的）构建科目匹配正则，
     * 这样管理员在后台新增科目分类后，解析器会自动识别，无需修改代码。
     */
    private void loadCategoryData() {
        allCategories = categoryService.list();
        childrenMap = new HashMap<>();
        for (Category c : allCategories) {
            Long parentId = c.getParentId() != null ? c.getParentId() : 0L;
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(c);
        }

        // 从子分类名称动态构建科目正则（排除"试卷"类分类）
        String subjectRegex = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() > 0)
                .filter(c -> !c.getName().endsWith("试卷"))
                .map(Category::getName)
                // 按名称长度降序排列，确保长名称（如"道德与法治"）优先匹配
                .sorted((a, b) -> b.length() - a.length())
                .collect(Collectors.joining("|"));
        if (!subjectRegex.isEmpty()) {
            subjectPattern = Pattern.compile("(" + subjectRegex + ")");
        } else {
            // 保底：至少匹配空
            subjectPattern = Pattern.compile("(.)", Pattern.LITERAL);
        }
    }

    /**
     * 加载标签缓存 + 关键词规则（从数据库读取）
     *
     * 1. TAG 类型规则 → 构建 关键词→标签名映射（取代硬编码的 TAG_KEYWORD_MAP）
     * 2. VERSION 类型规则 → 构建教材版本正则（取代硬编码的 VERSION_PATTERN）
     * 3. 加载标签 ID 缓存
     */
    private void loadKeywordRules() {
        // 1. 加载标签 ID 缓存
        List<Tag> tags = tagMapper.selectList(null);
        tagNameIdCache = new HashMap<>();
        for (Tag tag : tags) {
            tagNameIdCache.put(tag.getName(), tag.getId());
        }

        // 2. 从 keyword_rule 表加载启用的规则
        List<KeywordRule> rules = keywordRuleMapper.selectList(
                new LambdaQueryWrapper<KeywordRule>()
                        .eq(KeywordRule::getStatus, 1)
                        .orderByAsc(KeywordRule::getSort)
        );

        // 3. 构建标签关键词映射（type = 'TAG'）
        tagKeywordMap = new LinkedHashMap<>();
        // 收集 VERSION 规则的 target_name 去重，用最长的 keyword 作为匹配词
        Map<String, String> versionTargets = new LinkedHashMap<>();

        for (KeywordRule rule : rules) {
            if ("TAG".equals(rule.getType())) {
                tagKeywordMap.put(rule.getKeyword(), rule.getTargetName());
            } else if ("VERSION".equals(rule.getType())) {
                // 同一个版本名可能有多个关键词，取最长的作为正则匹配词
                String existing = versionTargets.get(rule.getTargetName());
                if (existing == null || rule.getKeyword().length() > existing.length()) {
                    versionTargets.put(rule.getTargetName(), rule.getKeyword());
                }
            }
        }

        // 4. 构建版本正则（按名称长度降序）
        if (!versionTargets.isEmpty()) {
            String versionRegex = versionTargets.values().stream()
                    .sorted((a, b) -> b.length() - a.length())
                    .map(Pattern::quote)
                    .collect(Collectors.joining("|"));
            versionPattern = Pattern.compile("(" + versionRegex + ")");
        } else {
            // 保底：没有任何版本规则时，不匹配任何内容
            versionPattern = Pattern.compile("(?!x)x");
        }
    }

    /**
     * 解析单个文本块
     */
    private ParsedResourceVo parseBlock(String block) {
        String[] lines = block.split("\\n");
        String title = "";
        String link = "";

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            // 跳过广告/装饰行
            if (line.startsWith("[") && line.endsWith("]")) continue;
            if (line.startsWith("（") && line.endsWith("）") && line.length() < 20) continue;

            Matcher linkMatcher = LINK_PATTERN.matcher(line);
            if (linkMatcher.find()) {
                link = linkMatcher.group();
            } else if (title.isEmpty() && line.length() > 2) {
                title = line;
            }
        }

        // 如果只有一行且包含链接，尝试从行中分离标题和链接
        if (title.isEmpty() && !link.isEmpty()) {
            title = link;
            link = "";
            Matcher m = LINK_PATTERN.matcher(title);
            if (m.find()) {
                link = m.group();
                title = title.replace(link, "").replaceAll("[\\s　\\\\[\\]]+", "").trim();
            }
        }

        if (title.isEmpty()) return null;

        return parseTitle(title, link);
    }

    /**
     * 解析标题行
     */
    private ParsedResourceVo parseTitle(String title, String link) {
        ParsedResourceVo vo = new ParsedResourceVo();
        vo.setTitle(title);
        vo.setNetdiskLink(link.isEmpty() ? null : link);

        // 提取书名《》
        Matcher bookMatcher = BOOK_PATTERN.matcher(title);
        if (bookMatcher.find()) {
            vo.setDescription(bookMatcher.group(1));
        }

        // 提取教材版本（从 keyword_rule 表动态加载）
        Matcher versionMatcher = versionPattern.matcher(title);
        if (versionMatcher.find()) {
            vo.setTextbookVersion(versionMatcher.group(1));
        }

        // 提取科目（从 category 表动态构建）
        Matcher subjectMatcher = subjectPattern.matcher(title);
        String subject = "";
        if (subjectMatcher.find()) {
            subject = subjectMatcher.group(1);
            vo.setSubject(subject);
        }

        // 提取年级
        int maxGrade = 0;
        Matcher gradeRangeMatcher = GRADE_RANGE_PATTERN.matcher(title);
        if (gradeRangeMatcher.find()) {
            int min = Integer.parseInt(gradeRangeMatcher.group(1));
            int max = Integer.parseInt(gradeRangeMatcher.group(2));
            maxGrade = max;
            vo.setGrade(min + "-" + max + "年级");
        } else if (title.contains("中考") && !title.contains("期中")) {
            vo.setGrade("中考");
        } else if (title.contains("高考")) {
            vo.setGrade("高考");
        }

        // 提取独立年级（如"高一""初一""一年级"），仅当尚未识别到年级范围时
        boolean hasRealZhongKao = title.contains("中考") && !title.contains("期中");
        if (maxGrade == 0 && !hasRealZhongKao && !title.contains("高考")) {
            Matcher sgMatcher = STANDALONE_GRADE_PATTERN.matcher(title);
            if (sgMatcher.find()) {
                String matchedGrade = sgMatcher.group(1);
                vo.setGrade(matchedGrade);
                // 根据中文年级设置 maxGrade 用于学段检测
                if (matchedGrade.matches("[一二三四五六]年级")) {
                    Map<String, Integer> map = Map.of("一",1,"二",2,"三",3,"四",4,"五",5,"六",6);
                    maxGrade = map.getOrDefault(String.valueOf(matchedGrade.charAt(0)), 0);
                } else if (matchedGrade.startsWith("初")) {
                    Map<String, Integer> map = Map.of("一",7,"二",8,"三",9);
                    maxGrade = map.getOrDefault(String.valueOf(matchedGrade.charAt(1)), 7);
                } else if (matchedGrade.startsWith("高")) {
                    Map<String, Integer> map = Map.of("一",10,"二",11,"三",12);
                    maxGrade = map.getOrDefault(String.valueOf(matchedGrade.charAt(1)), 10);
                }
            } else {
                // 尝试阿拉伯数字年级（如"7年级""8年级"）
                Matcher agMatcher = ARABIC_GRADE_PATTERN.matcher(title);
                if (agMatcher.find()) {
                    int gradeNum = Integer.parseInt(agMatcher.group(1));
                    vo.setGrade(gradeNum + "年级");
                    maxGrade = gradeNum; // 用于 matchCategory 的学段检测
                }
            }
        }

        // 匹配分类
        matchCategory(vo, subject, maxGrade);

        // 自动检测标签
        detectTags(vo, title);

        return vo;
    }

    /**
     * 根据科目和年级匹配分类
     */
    private void matchCategory(ParsedResourceVo vo, String subject, int maxGrade) {
        // 确定学段
        String levelName = null;
        String title = vo.getTitle();

        if (title.contains("高考")) {
            levelName = "高考";
        } else if (title.contains("中考") && !title.contains("期中")) {
            levelName = "中考";
        } else if (maxGrade > 0) {
            if (maxGrade <= 6) {
                levelName = "小学";
            } else if (maxGrade <= 9) {
                levelName = "初中";
            } else {
                levelName = "高中";
            }
        }

        // 从独立年级确定学段（如"高一"→高中，"初一"→初中）
        if (levelName == null) {
            Matcher sgMatcher = STANDALONE_GRADE_PATTERN.matcher(title);
            if (sgMatcher.find()) {
                String grade = sgMatcher.group(1);
                if (grade.matches("[一二三四五六]年级")) {
                    levelName = "小学";
                } else if (grade.startsWith("初")) {
                    levelName = "初中";
                } else if (grade.startsWith("高")) {
                    levelName = "高中";
                }
            }
        }

        if (levelName == null) {
            // 无法通过学段判断，尝试试卷类资源匹配（如"高一全科试卷"→"高一试卷"）
            if (subject.isEmpty() && hasExamKeywords(title)) {
                matchExamPaperCategory(vo, title);
            }
            if (vo.getCategoryId() != null) return;
            // 尝试直接在科目匹配的分类下找
            if (!subject.isEmpty()) {
                for (Category c : allCategories) {
                    if (c.getName().equals(subject) && c.getParentId() != null && c.getParentId() > 0) {
                        vo.setCategoryId(c.getId());
                        // 找父级名称
                        Category parent = findCategoryById(c.getParentId());
                        if (parent != null) {
                            vo.setCategoryName(parent.getName() + "/" + c.getName());
                        } else {
                            vo.setCategoryName(c.getName());
                        }
                        return;
                    }
                }
            }
            return;
        }

        // 找到对应的顶层分类
        List<Category> topList = childrenMap.getOrDefault(0L, Collections.emptyList());
        for (Category top : topList) {
            if (top.getName().equals(levelName)) {
                if (!subject.isEmpty()) {
                    // 在子分类中查找匹配的科目
                    List<Category> children = childrenMap.getOrDefault(top.getId(), Collections.emptyList());
                    for (Category child : children) {
                        if (child.getName().equals(subject)) {
                            vo.setCategoryId(child.getId());
                            vo.setCategoryName(top.getName() + "/" + child.getName());
                            return;
                        }
                    }
                }
                // 无具体科目，尝试匹配试卷类资源（如"高一全科试卷"→"高一试卷"）
                if (hasExamKeywords(title)) {
                    matchExamPaperCategory(vo, title);
                    if (vo.getCategoryId() != null) return;
                }
                // 未匹配到子分类，使用顶层分类
                vo.setCategoryId(top.getId());
                vo.setCategoryName(top.getName());
                return;
            }
        }
    }

    /**
     * 匹配试卷类资源到对应的年级试卷分类（如"高一"→"高一试卷"）
     */
    private void matchExamPaperCategory(ParsedResourceVo vo, String title) {
        Matcher gradeMatcher = STANDALONE_GRADE_PATTERN.matcher(title);
        if (gradeMatcher.find()) {
            String gradeText = gradeMatcher.group(1);
            String examCatName = gradeText + "试卷";
            for (Category c : allCategories) {
                if (c.getName().equals(examCatName)) {
                    vo.setCategoryId(c.getId());
                    Category parent = findCategoryById(c.getParentId());
                    vo.setCategoryName(parent != null ? parent.getName() + "/" + c.getName() : c.getName());
                    vo.setSubject("全科");
                    return;
                }
            }
        }
    }

    /**
     * 判断是否是试卷类资源（无具体科目时走年级试卷分类）
     */
    private boolean hasExamKeywords(String title) {
        return title.contains("全科") || title.contains("试卷") ||
               title.contains("月考") || title.contains("联考") ||
               title.contains("质量检测") || title.contains("模拟");
    }

    /**
     * 根据标题关键词自动检测标签
     */
    private void detectTags(ParsedResourceVo vo, String title) {
        Set<Long> matchedIds = new LinkedHashSet<>();
        for (Map.Entry<String, String> entry : tagKeywordMap.entrySet()) {
            if (title.contains(entry.getKey())) {
                Long tagId = tagNameIdCache.get(entry.getValue());
                if (tagId != null) {
                    matchedIds.add(tagId);
                }
            }
        }
        if (!matchedIds.isEmpty()) {
            vo.setTagIds(new ArrayList<>(matchedIds));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchImport(List<ParsedResourceVo> resources) {
        for (ParsedResourceVo vo : resources) {
            // 跳过已存在的资源
            if (Boolean.TRUE.equals(vo.getAlreadyExists())) continue;
            if (vo.getTitle() == null || vo.getTitle().trim().isEmpty()) continue;
            if (vo.getCategoryId() == null) continue;

            Resource resource = new Resource();
            resource.setTitle(vo.getTitle().trim());
            resource.setDescription(vo.getDescription() != null ? vo.getDescription() : "");
            resource.setCategoryId(vo.getCategoryId());
            resource.setNetdiskLink(vo.getNetdiskLink());
            resource.setNetdiskCode(vo.getNetdiskCode());
            resource.setViewCount(0);
            resource.setDownloadCount(0);
            resource.setIsRecommend(0);
            resource.setStatus(1); // 直接发布
            resource.setSort(0);

            resourceMapper.insert(resource);

            // 保存标签关联
            if (vo.getTagIds() != null && !vo.getTagIds().isEmpty()) {
                for (Long tagId : vo.getTagIds()) {
                    if (tagId != null) {
                        ResourceTag rt = new ResourceTag();
                        rt.setResourceId(resource.getId());
                        rt.setTagId(tagId);
                        resourceTagMapper.insert(rt);
                    }
                }
            }
        }
    }

    private Category findCategoryById(Long id) {
        for (Category c : allCategories) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }
}
