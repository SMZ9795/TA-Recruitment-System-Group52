package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.util.ValidationUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Smart skill-matching and job recommendation engine.
 *
 * <p>Matching uses a four-layer strategy applied in order of confidence:
 * <ol>
 *   <li><b>Exact match</b> – after synonym resolution both sides map to the same canonical form.</li>
 *   <li><b>Substring match</b> – one canonical form contains the other (e.g. "core java" → "java").</li>
 *   <li><b>Related-skill match</b> – applicant owns a skill that is academically related (partial credit).</li>
 *   <li><b>Fuzzy match</b> – Levenshtein edit-distance catches typos (e.g. "pythn" → "python").</li>
 * </ol>
 *
 * <p>The recommendation engine additionally factors in weekly-hour availability and applies a
 * hard cutoff when a TA physically cannot take on more work.
 */
public class AiMatchingService {

    // =====================================================================
    //  Match-layer weights – how much credit each layer gives
    // =====================================================================
    private static final double WEIGHT_EXACT     = 1.0;
    private static final double WEIGHT_SUBSTRING = 0.85;
    private static final double WEIGHT_RELATED   = 0.50;
    private static final double WEIGHT_FUZZY     = 0.75;

    // =====================================================================
    //  Synonym dictionary – maps common aliases to a canonical form
    // =====================================================================
    private static final Map<String, String> SYNONYM_MAP = new HashMap<>();

    static {
        // Programming languages
        syn("java",        "core java", "java se", "java ee", "jdk", "jre");
        syn("python",      "py", "python3", "python2", "cpython");
        syn("javascript",  "js", "ecmascript", "es6", "es5", "es2015");
        syn("typescript",  "ts");
        syn("cpp",         "c++", "cplusplus", "c plus plus");
        syn("csharp",      "c#", "c sharp", "dotnet", ".net");
        syn("golang",      "go", "go lang");
        syn("r language",  "r", "rlang");
        syn("matlab",      "mat lab");
        syn("kotlin",      "kt");
        syn("swift",       "swiftlang");
        syn("rust",        "rustlang");
        syn("ruby",        "rb");
        syn("scala",       "sc");
        syn("php",         "php7", "php8");

        // Data science & AI
        syn("machine learning",           "ml", "machine-learning", "machinelearning");
        syn("deep learning",              "dl", "deep-learning", "deeplearning");
        syn("artificial intelligence",    "ai", "a.i.", "a i");
        syn("natural language processing","nlp", "natural-language-processing");
        syn("computer vision",            "cv", "image recognition");
        syn("data analysis",              "data analytics", "data-analysis", "dataanalysis");
        syn("data science",               "datascience", "data-science");
        syn("neural networks",            "nn", "neural network", "neural net");

        // Database & data
        syn("database",   "db", "databases", "dbms", "rdbms");
        syn("sql",         "mysql", "postgresql", "postgres", "sqlite", "tsql", "pl/sql", "plsql");
        syn("nosql",       "mongodb", "mongo", "couchdb", "cassandra", "dynamodb");
        syn("redis",       "redis db");

        // Web technologies
        syn("html",        "html5", "hypertext");
        syn("css",         "css3", "stylesheet", "stylesheets");
        syn("react",       "reactjs", "react.js", "react js");
        syn("vue",         "vuejs", "vue.js", "vue js");
        syn("angular",     "angularjs", "angular.js", "angular js");
        syn("nodejs",      "node", "node.js", "node js");
        syn("spring",      "spring boot", "springboot", "spring-boot", "spring framework");
        syn("django",      "django framework");
        syn("flask",       "flask framework");
        syn("express",     "expressjs", "express.js");

        // Testing & QA
        syn("testing",     "software testing", "test", "qa", "quality assurance");
        syn("junit",       "j-unit", "j unit", "junit5", "junit4");
        syn("unit testing","unit test", "unit-testing", "unittesting");
        syn("selenium",    "selenium webdriver");

        // DevOps & tools
        syn("docker",      "containerization", "containers");
        syn("kubernetes",  "k8s");
        syn("git",         "github", "gitlab", "version control", "vcs");
        syn("linux",       "unix", "ubuntu", "centos", "debian", "fedora");
        syn("bash",        "shell", "shell scripting", "sh");
        syn("ci/cd",       "cicd", "ci cd", "continuous integration");

        // Math & science
        syn("mathematics", "math", "maths");
        syn("statistics",  "stats", "stat", "statistical analysis");
        syn("calculus",    "calc", "integral calculus", "differential calculus");
        syn("linear algebra", "linear-algebra", "linalg");
        syn("probability", "prob", "probability theory");
        syn("discrete math", "discrete mathematics", "discrete-math");

        // CS fundamentals
        syn("algorithms",          "algorithm", "algo", "algos");
        syn("data structures",     "data structure", "ds", "data-structures");
        syn("oop",                 "object oriented programming", "object-oriented", "object oriented");
        syn("operating systems",   "os", "operating system");
        syn("computer networks",   "networking", "networks", "computer network");
        syn("software engineering","se", "software eng");
        syn("design patterns",     "design pattern", "gof patterns");

        // Soft skills
        syn("communication",      "communication skills", "verbal communication", "written communication");
        syn("tutoring",           "tutor", "tutorship", "teaching assistant");
        syn("presentation",       "public speaking", "presentations");
        syn("teamwork",           "team work", "collaboration", "team player");
    }

    // =====================================================================
    //  Related-skills graph – partial credit for academically related skills
    // =====================================================================
    private static final Map<String, Set<String>> RELATED_SKILLS = new LinkedHashMap<>();

    static {
        rel("java",         "junit", "oop", "testing", "spring", "algorithms", "data structures", "kotlin");
        rel("python",       "machine learning", "data analysis", "data science", "statistics",
                            "algorithms", "flask", "django", "numpy");
        rel("javascript",   "html", "css", "nodejs", "react", "vue", "angular", "typescript");
        rel("typescript",   "javascript", "html", "css", "angular", "react");
        rel("cpp",          "algorithms", "data structures", "oop", "operating systems");
        rel("csharp",       "oop", "testing", "algorithms");
        rel("machine learning", "python", "statistics", "deep learning", "artificial intelligence",
                                "data analysis", "linear algebra", "r language", "probability");
        rel("deep learning","machine learning", "python", "linear algebra", "neural networks");
        rel("artificial intelligence", "machine learning", "deep learning", "python", "statistics");
        rel("sql",          "database", "data analysis", "nosql");
        rel("database",     "sql", "nosql", "data structures");
        rel("nosql",        "database", "sql");
        rel("algorithms",   "data structures", "java", "python", "cpp", "discrete math", "mathematics");
        rel("data structures", "algorithms", "java", "python", "cpp");
        rel("testing",      "junit", "java", "unit testing", "selenium");
        rel("junit",        "java", "testing", "unit testing");
        rel("html",         "css", "javascript");
        rel("css",          "html", "javascript");
        rel("react",        "javascript", "html", "css", "typescript", "nodejs");
        rel("vue",          "javascript", "html", "css");
        rel("angular",      "javascript", "typescript", "html", "css");
        rel("nodejs",       "javascript", "express");
        rel("spring",       "java", "oop", "testing");
        rel("linux",        "operating systems", "bash", "docker");
        rel("bash",         "linux", "operating systems");
        rel("docker",       "linux", "kubernetes", "ci/cd");
        rel("kubernetes",   "docker", "linux", "ci/cd");
        rel("git",          "ci/cd", "software engineering");
        rel("calculus",     "mathematics", "linear algebra", "statistics", "probability");
        rel("linear algebra","mathematics", "calculus", "machine learning", "statistics");
        rel("statistics",   "mathematics", "data analysis", "machine learning", "python", "r language", "probability");
        rel("probability",  "mathematics", "statistics", "machine learning");
        rel("data analysis","statistics", "python", "sql", "r language");
        rel("data science", "machine learning", "python", "statistics", "data analysis", "sql");
        rel("oop",          "java", "cpp", "csharp", "design patterns");
        rel("operating systems", "linux", "cpp", "bash");
        rel("communication","tutoring", "presentation", "teamwork");
        rel("tutoring",     "communication", "presentation");
        rel("software engineering", "oop", "testing", "design patterns", "git");
        rel("r language",   "statistics", "data analysis", "data science");
    }

    // =====================================================================
    //  Static initializer helpers
    // =====================================================================

    private static void syn(String canonical, String... aliases) {
        String key = canonical.toLowerCase().trim();
        SYNONYM_MAP.put(key, key);
        for (String alias : aliases) {
            SYNONYM_MAP.put(alias.toLowerCase().trim(), key);
        }
    }

    private static void rel(String skill, String... related) {
        String canon = toCanonical(skill);
        Set<String> set = RELATED_SKILLS.computeIfAbsent(canon, k -> new LinkedHashSet<>());
        for (String r : related) {
            set.add(toCanonical(r));
        }
    }

    // =====================================================================
    //  Normalization & canonicalization
    // =====================================================================

    /** Normalize a raw skill token for comparison (lowercase, collapse whitespace). */
    private static String normalize(String skill) {
        return skill.toLowerCase().trim()
                .replaceAll("[\\-_.]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /** Resolve a skill to its canonical synonym, or return the normalized form as-is. */
    private static String toCanonical(String skill) {
        String normalized = normalize(skill);
        return SYNONYM_MAP.getOrDefault(normalized, normalized);
    }

    // =====================================================================
    //  Tokenizer
    // =====================================================================

    private Set<String> tokenizeSkills(String rawSkills) {
        Set<String> skills = new LinkedHashSet<>();
        if (rawSkills == null || rawSkills.isBlank()) {
            return skills;
        }
        String[] parts = rawSkills.split("[,;|/\\n\\r\\t]");
        for (String part : parts) {
            String cleaned = part.trim().toLowerCase();
            if (!cleaned.isEmpty()) {
                skills.add(cleaned);
            }
        }
        return skills;
    }

    // =====================================================================
    //  Core matching – 4-layer strategy
    // =====================================================================

    public MatchResult analyzeSkills(String applicantSkillsRaw, String requiredSkillsRaw) {
        Set<String> applicantTokens  = tokenizeSkills(applicantSkillsRaw);
        Set<String> requiredTokens   = tokenizeSkills(requiredSkillsRaw);

        if (requiredTokens.isEmpty()) {
            return new MatchResult(100, List.of(), List.of(),
                    "No required skills were specified for this job.");
        }

        // Pre-compute applicant canonical forms and related skills
        Set<String> applicantCanonical = new LinkedHashSet<>();
        for (String token : applicantTokens) {
            applicantCanonical.add(toCanonical(token));
        }

        Set<String> applicantRelated = new LinkedHashSet<>();
        for (String canon : applicantCanonical) {
            Set<String> rels = RELATED_SKILLS.get(canon);
            if (rels != null) {
                applicantRelated.addAll(rels);
            }
        }

        List<String> matchedSkills  = new ArrayList<>();
        List<String> missingSkills  = new ArrayList<>();
        double totalWeight = 0.0;

        for (String reqToken : requiredTokens) {
            String reqCanon = toCanonical(reqToken);
            LayerResult best = findBestMatch(reqCanon, applicantCanonical, applicantTokens, applicantRelated);

            switch (best.layer) {
                case EXACT -> {
                    matchedSkills.add(reqToken);
                    totalWeight += WEIGHT_EXACT;
                }
                case SUBSTRING -> {
                    matchedSkills.add(reqToken + " (≈" + best.matchedVia + ")");
                    totalWeight += WEIGHT_SUBSTRING;
                }
                case RELATED -> {
                    matchedSkills.add(reqToken + " (related: " + best.matchedVia + ")");
                    totalWeight += WEIGHT_RELATED;
                }
                case FUZZY -> {
                    matchedSkills.add(reqToken + " (≈" + best.matchedVia + ")");
                    totalWeight += WEIGHT_FUZZY;
                }
                case NONE -> {
                    missingSkills.add(reqToken);
                }
            }
        }

        int score = (int) Math.round((totalWeight / requiredTokens.size()) * 100.0);
        score = Math.max(0, Math.min(100, score));

        // Build human-readable reason
        int exactCount    = (int) matchedSkills.stream().filter(s -> !s.contains("(")).count();
        int partialCount  = matchedSkills.size() - exactCount;

        StringBuilder reason = new StringBuilder();
        reason.append("Matched ").append(matchedSkills.size())
              .append(" of ").append(requiredTokens.size()).append(" required skills");
        if (partialCount > 0) {
            reason.append(" (").append(exactCount).append(" exact, ")
                  .append(partialCount).append(" via smart matching)");
        }
        reason.append(".");

        return new MatchResult(score, matchedSkills, missingSkills, reason.toString());
    }

    // =====================================================================
    //  4-layer match resolution
    // =====================================================================

    private LayerResult findBestMatch(String reqCanon,
                                      Set<String> applicantCanonical,
                                      Set<String> applicantTokens,
                                      Set<String> applicantRelated) {

        // Layer 1: Exact canonical match
        if (applicantCanonical.contains(reqCanon)) {
            return new LayerResult(MatchLayer.EXACT, reqCanon);
        }

        // Layer 2: Substring match – one contains the other
        for (String appCanon : applicantCanonical) {
            if (reqCanon.length() >= 2 && appCanon.length() >= 2) {
                if (appCanon.contains(reqCanon) || reqCanon.contains(appCanon)) {
                    return new LayerResult(MatchLayer.SUBSTRING, appCanon);
                }
            }
        }
        // Also try raw tokens for substring (before canonicalization)
        for (String appToken : applicantTokens) {
            String normApp = normalize(appToken);
            if (reqCanon.length() >= 2 && normApp.length() >= 2) {
                if (normApp.contains(reqCanon) || reqCanon.contains(normApp)) {
                    return new LayerResult(MatchLayer.SUBSTRING, appToken);
                }
            }
        }

        // Layer 3: Related-skill match
        if (applicantRelated.contains(reqCanon)) {
            // Find which applicant skill caused the relation for display
            String via = "related skill";
            for (String appCanon : applicantCanonical) {
                Set<String> rels = RELATED_SKILLS.get(appCanon);
                if (rels != null && rels.contains(reqCanon)) {
                    via = appCanon;
                    break;
                }
            }
            return new LayerResult(MatchLayer.RELATED, via);
        }

        // Layer 4: Fuzzy match (Levenshtein distance) – skip very short words to avoid false positives
        if (reqCanon.length() >= 3) {
            for (String appCanon : applicantCanonical) {
                if (appCanon.length() >= 3 && isFuzzyMatch(reqCanon, appCanon)) {
                    return new LayerResult(MatchLayer.FUZZY, appCanon);
                }
            }
        }

        return new LayerResult(MatchLayer.NONE, null);
    }

    // =====================================================================
    //  Fuzzy matching via Levenshtein distance
    // =====================================================================

    private boolean isFuzzyMatch(String a, String b) {
        if (a.equals(b)) return true;
        // Don't fuzzy-match words that are wildly different in length
        if (Math.abs(a.length() - b.length()) > 3) return false;
        int maxDist = Math.min(a.length(), b.length()) <= 5 ? 1 : 2;
        return levenshteinDistance(a, b) <= maxDist;
    }

    private int levenshteinDistance(String a, String b) {
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
            }
        }
        return dp[m][n];
    }

    // =====================================================================
    //  Job recommendation
    // =====================================================================

    public RecommendationResult recommendJob(User ta, Job job, int acceptedHours) {
        if (ta == null || job == null) {
            return new RecommendationResult(0, "Low Fit",
                    "TA profile or job data is missing.",
                    false, 0,
                    "Review profile and job data before applying.");
        }
        if (job.getStatus() != JobStatus.OPEN) {
            return new RecommendationResult(0, "Unavailable",
                    "This job is not open for applications.",
                    false, 0,
                    "Choose another open job.");
        }

        MatchResult matchResult = analyzeSkills(ta.getSkills(), job.getRequiredSkills());
        int remainingHours = Math.max(0, ta.getAvailableHours() - Math.max(0, acceptedHours));
        boolean availabilityNotSet = ta.getAvailableHours() <= 0;
        boolean hoursFit = availabilityNotSet || job.getHoursPerWeek() <= remainingHours;

        int score = matchResult.getScore();

        // ---- Workload hard cutoff ----
        // If the TA physically cannot take this job, cap score and label immediately
        if (!hoursFit && !availabilityNotSet) {
            score = Math.min(score, 25);
            String reason = matchResult.getReason()
                    + " Hours conflict: only " + remainingHours + "h/week available but this job needs "
                    + job.getHoursPerWeek() + "h/week.";
            return new RecommendationResult(score, "Not Available", reason,
                    false, remainingHours,
                    "You do not have enough available hours for this job. Consider withdrawing from another position first.");
        }

        // ---- Normal scoring with workload bonus ----
        if (hoursFit && !availabilityNotSet) {
            // Bonus proportional to how well the hours fit
            double fitRatio = (double) remainingHours / Math.max(1, ta.getAvailableHours());
            int bonus = (int) Math.round(fitRatio * 10);
            score = Math.min(100, score + bonus);
        }

        // ---- Determine recommendation label ----
        String label;
        if (score >= 80 && hoursFit) {
            label = "Recommended";
        } else if (score >= 50) {
            label = "Review";
        } else {
            label = "Low Fit";
        }

        // ---- Build detailed reason text ----
        String matched = matchResult.getMatchedSkills().isEmpty()
                ? "none"
                : String.join(", ", matchResult.getMatchedSkills());
        String missing = matchResult.getMissingSkills().isEmpty()
                ? "none"
                : String.join(", ", matchResult.getMissingSkills());
        String hoursDetail = availabilityNotSet
                ? "Availability is not set, so this is treated as flexible but should be confirmed."
                : "Hours fit: " + remainingHours + "h/week remaining for a "
                        + job.getHoursPerWeek() + "h/week job.";

        String actionHint;
        if ("Recommended".equals(label)) {
            actionHint = "Strong match: apply first if the module is interesting.";
        } else if ("Review".equals(label)) {
            actionHint = "Review before applying: check the missing skills or weekly hours.";
        } else {
            actionHint = "Lower priority: consider improving the profile match or choosing another job.";
        }

        String reason = matchResult.getReason()
                + " Matched skills: " + matched + "."
                + " Missing skills: " + missing + "."
                + " " + hoursDetail;
        return new RecommendationResult(score, label, reason, hoursFit, remainingHours, actionHint);
    }

    // =====================================================================
    //  Internal types
    // =====================================================================

    private enum MatchLayer { EXACT, SUBSTRING, RELATED, FUZZY, NONE }

    private static final class LayerResult {
        final MatchLayer layer;
        final String matchedVia;
        LayerResult(MatchLayer layer, String matchedVia) {
            this.layer = layer;
            this.matchedVia = matchedVia;
        }
    }

    // =====================================================================
    //  Public result classes (API-compatible with the rest of the system)
    // =====================================================================

    public static final class MatchResult {
        private final int score;
        private final List<String> matchedSkills;
        private final List<String> missingSkills;
        private final String reason;

        public MatchResult(int score, List<String> matchedSkills, List<String> missingSkills, String reason) {
            this.score = ValidationUtil.parseIntInRange(String.valueOf(score), "Match score", 0, 100);
            this.matchedSkills = List.copyOf(matchedSkills == null ? List.of() : matchedSkills);
            this.missingSkills = List.copyOf(missingSkills == null ? List.of() : missingSkills);
            this.reason = reason == null ? "" : reason;
        }

        public int getScore() {
            return score;
        }

        public List<String> getMatchedSkills() {
            return matchedSkills;
        }

        public List<String> getMissingSkills() {
            return missingSkills;
        }

        public String getReason() {
            return reason;
        }
    }

    public static final class RecommendationResult {
        private final int score;
        private final String label;
        private final String reason;
        private final boolean hoursFit;
        private final int remainingHours;
        private final String actionHint;

        public RecommendationResult(int score, String label, String reason) {
            this(score, label, reason, false, 0, "");
        }

        public RecommendationResult(
                int score, String label, String reason, boolean hoursFit, int remainingHours, String actionHint) {
            this.score = ValidationUtil.parseIntInRange(String.valueOf(score), "Recommendation score", 0, 100);
            this.label = label == null ? "" : label;
            this.reason = reason == null ? "" : reason;
            this.hoursFit = hoursFit;
            this.remainingHours = Math.max(0, remainingHours);
            this.actionHint = actionHint == null ? "" : actionHint;
        }

        public int getScore() {
            return score;
        }

        public String getLabel() {
            return label;
        }

        public String getReason() {
            return reason;
        }

        public boolean isRecommended() {
            return "Recommended".equalsIgnoreCase(label);
        }

        public boolean isHoursFit() {
            return hoursFit;
        }

        public int getRemainingHours() {
            return remainingHours;
        }

        public String getActionHint() {
            return actionHint;
        }
    }
}
