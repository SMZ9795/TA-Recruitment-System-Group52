package com.group52.tarecruitment;

import java.io.*;
import java.util.*;


public class TAApplyModule {

    private static final String FILE_PATH = "src/com/group52/tarecruitment/data/jobs.json";
    private List<Job> jobsList = new ArrayList<>();

    // 内部类：职位实体
    static class Job {
        int id;
        String title;
        String description;
        String status;
        String applicant;

        public Job(int id, String title, String description, String status) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.status = status;
            this.applicant = "";
        }

        @Override
        public String toString() {
            return "职位ID: " + id + ", 标题: " + title + ", 状态: " + status + ", 申请者: " + applicant;
        }
    }

    public static void main(String[] args) {
        TAApplyModule module = new TAApplyModule();
        module.loadJobs(); // 加载数据
        module.displayAvailableJobs(); // 展示职位

        // 模拟申请：假设 TA ID 是 "TA001"，申请职位 ID 为 1
        boolean success = module.applyForJob("TA001", "1");
        if (success) {
            System.out.println("申请成功！正在保存数据...");
            module.saveJobs(); // 保存回文件
        }
    }

    public void loadJobs() {
        jobsList.clear();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            System.out.println("数据文件不存在，初始化空列表。");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line).append("\n");
            }

            // 简单解析 JSON 数组（假设格式为 [{"id":1,...},...])
            String jsonStr = jsonContent.toString();
            // 移除首尾的方括号
            if (jsonStr.startsWith("[")) {
                jsonStr = jsonStr.substring(1);
            }
            if (jsonStr.endsWith("]")) {
                jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
            }

            // 按逗号分隔对象（简易解析，实际项目建议用 JSON 库）
            String[] entries = jsonStr.split("\\},\\{");
            for (String entry : entries) {
                entry = entry.trim();
                if (!entry.isEmpty()) {
                    // 补回花括号以便解析
                    String objStr = "{" + entry + "}";
                    // 提取字段（这里用正则或字符串查找，简单演示）
                    int id = extractInt(objStr, "\"id\":");
                    String title = extractString(objStr, "\"title\":");
                    String desc = extractString(objStr, "\"description\":");
                    String status = extractString(objStr, "\"status\":");
                    String applicant = extractString(objStr, "\"applicant\":");

                    jobsList.add(new Job(id, title, desc, status));
                }
            }

            System.out.println("成功加载 " + jobsList.size() + " 个职位。");

        } catch (IOException e) {
            System.err.println("读取文件时出错: " + e.getMessage());
        }
    }

    private int extractInt(String json, String key) {
        Pattern pattern = Pattern.compile(key + "\"?:(\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

 
    private String extractString(String json, String key) {
        Pattern pattern = Pattern.compile(key + "\"?:\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

 
    public void displayAvailableJobs() {
        System.out.println("\n--- 当前开放职位 ---");
        boolean hasOpen = false;
        for (Job job : jobsList) {
            if (job.status.equalsIgnoreCase("Open")) {
                hasOpen = true;
                System.out.println("ID: " + job.id + " | " + job.title + " | " + job.description);
            }
        }
        if (!hasOpen) {
            System.out.println("暂无开放职位。");
        }
    }

   
    public boolean applyForJob(String taId, String jobId) {
        // 校验1：检查岗位是否存在且开放 (Exist & Open)
        Job targetJob = findJobById(jobId);
        if (targetJob == null) {
            System.out.println("错误：岗位不存在。");
            return false;
        }

        if (!targetJob.status.equalsIgnoreCase("Open")) {
            System.out.println("错误：该岗位已截止，无法申请。");
            return false;
        }

        // 校验2：重复申请校验 (Duplicate Check)
        if (hasApplied(taId, jobId)) {
            System.out.println("错误：您已申请过该岗位 [" + targetJob.title + "]，请勿重复申请。");
            return false;
        }

        // 执行申请：标记申请者和状态
        targetJob.applicant = taId;
        targetJob.status = "Applied"; // 或者 "Pending"
        System.out.println("申请成功！您已申请职位：" + targetJob.title);
        return true;
    }


    private Job findJobById(String jobId) {
        for (Job job : jobsList) {
            if (String.valueOf(job.id).equals(jobId)) {
                return job;
            }
        }
        return null;
    }


    private boolean hasApplied(String taId, String jobId) {
        Job job = findJobById(jobId);
        if (job != null && job.applicant != null && job.applicant.equals(taId)) {
            return true;
        }
        return false;
    }

  
    public void saveJobs() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            writer.print("["); // JSON 数组开始

            for (int i = 0; i < jobsList.size(); i++) {
                Job job = jobsList.get(i);
                writer.print("{");
                writer.print("\"id\":" + job.id + ",");
                writer.print("\"title\":\"" + job.title + "\",");
                writer.print("\"description\":\"" + job.description + "\",");
                writer.print("\"status\":\"" + job.status + "\",");
                writer.print("\"applicant\":\"" + job.applicant + "\"");
                writer.print("}");

                if (i < jobsList.size() - 1) {
                    writer.print(","); // 逗号分隔
                }
            }

            writer.print("]"); // JSON 数组结束
            System.out.println("数据已成功保存到 " + FILE_PATH);

        } catch (IOException e) {
            System.err.println("写入文件时出错: " + e.getMessage());
        }
    }
}
