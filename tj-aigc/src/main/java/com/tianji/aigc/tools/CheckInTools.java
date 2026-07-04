package com.tianji.aigc.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class CheckInTools {

    @Tool(description = "学习打卡，记录今日学习。完成打卡后可获得激励语和学习建议。")
    public String checkIn(
            @ToolParam(description = "今日学习时长，单位：分钟") Integer studyMinutes,
            @ToolParam(description = "今日学习的课程ID") Long courseId,
            @ToolParam(description = "今日学习心得") String note) {
        try {
            LocalDate today = LocalDate.now();

            StringBuilder result = new StringBuilder();
            result.append("## ✅ 打卡成功！\n\n");
            result.append("📅 **日期**：").append(today).append("\n");
            if (studyMinutes != null) {
                result.append("⏱️ **学习时长**：").append(studyMinutes).append(" 分钟\n");
            }
            if (courseId != null) {
                result.append("📚 **学习课程**：课程 ID ").append(courseId).append("\n");
            }
            result.append("\n");

            List<String> encouragements = List.of(
                    "太棒了！今天的你闪闪发光！✨",
                    "坚持就是胜利，继续加油！💪",
                    "学习使你进步，今天又收获满满！🎉",
                    "每一次坚持都是向更好的自己迈进！🚀",
                    "知识就是力量，今天你又充满了力量！⚡",
                    "今天也超棒的！为自己鼓掌！👏",
                    "学无止境，你走在正确的路上！🌟",
                    "优秀是一种习惯，你正在养成它！🌈"
            );
            Random random = new Random();
            result.append("### 🎁 今日寄语\n");
            result.append("> ").append(encouragements.get(random.nextInt(encouragements.size()))).append("\n\n");

            result.append("### 🔥 连续打卡\n");
            result.append("保持连续打卡，解锁更多成就！\n\n");

            result.append("### 💡 明日建议\n");
            List<String> tips = List.of(
                    "提前规划明天的学习内容，效率更高~",
                    "学完后及时复习，事半功倍哦~",
                    "找一个学习伙伴，互相监督更有动力~",
                    "把学到的知识讲给别人听，检验自己的理解~",
                    "适当休息，劳逸结合才是王道~"
            );
            result.append(tips.get(random.nextInt(tips.size()))).append("\n\n");

            if (note != null && !note.isEmpty()) {
                result.append("### 📝 今日学习心得\n");
                result.append("> ").append(note).append("\n\n");
            }

            result.append("明天也要继续加油哦！我会一直陪着你的 (≧∇≦)ﾉ");

            return result.toString();
        } catch (Exception e) {
            log.error("打卡失败", e);
            return "打卡时发生错误：" + e.getMessage();
        }
    }

    @Tool(description = "获取学习打卡日历，展示本月打卡情况和连续打卡天数。")
    public String getCheckInCalendar(
            @ToolParam(description = "年份，默认今年") Integer year,
            @ToolParam(description = "月份，默认本月") Integer month) {
        try {
            LocalDate today = LocalDate.now();
            int y = year != null ? year : today.getYear();
            int m = month != null ? month : today.getMonthValue();

            StringBuilder result = new StringBuilder();
            result.append("## 📆 学习打卡日历\n\n");
            result.append("### ").append(y).append("年").append(m).append("月\n\n");

            result.append("| 一 | 二 | 三 | 四 | 五 | 六 | 日 |\n");
            result.append("|----|----|----|----|----|----|----|\n");

            LocalDate firstDay = LocalDate.of(y, m, 1);
            int firstDayOfWeek = firstDay.getDayOfWeek().getValue();

            int daysInMonth = firstDay.lengthOfMonth();
            int dayCount = 1;

            for (int week = 0; week < 6; week++) {
                result.append("|");
                for (int day = 0; day < 7; day++) {
                    if (week == 0 && day < firstDayOfWeek - 1) {
                        result.append("   ");
                    } else if (dayCount <= daysInMonth) {
                        boolean isToday = dayCount == today.getDayOfMonth() && m == today.getMonthValue() && y == today.getYear();
                        String symbol = isToday ? "**" + dayCount + "**" : String.valueOf(dayCount);
                        result.append(" ").append(symbol).append(" ");
                        dayCount++;
                    } else {
                        result.append("   ");
                    }
                    result.append("|");
                }
                result.append("\n");
                if (dayCount > daysInMonth) break;
            }

            result.append("\n### 🌟 打卡统计\n\n");
            result.append("- **本月打卡**：需要你坚持哦~\n");
            result.append("- **连续打卡**：0 天\n");
            result.append("- **累计打卡**：0 天\n\n");

            result.append("### 🎯 打卡目标\n\n");
            result.append("- 本月目标：30 天\n");
            result.append("- 当前进度：0%\n\n");

            result.append("今天打卡了吗？如果还没有，现在就开始吧！(^ω^)");

            return result.toString();
        } catch (Exception e) {
            log.error("获取打卡日历失败", e);
            return "获取打卡日历时发生错误：" + e.getMessage();
        }
    }

    @Tool(description = "获取学习激励，当学习遇到困难或想放弃时，调用此工具获取鼓励和动力。")
    public String getEncouragement(
            @ToolParam(description = "当前心情/状态，例如：累了、不想学了、太难了") String mood) {
        try {
            StringBuilder result = new StringBuilder();
            result.append("## 💪 加油！\n\n");

            List<String[]> messages = new ArrayList<>();

            messages.add(new String[]{"累了",
                    "学习累了就休息一下吧~ 适当的休息是为了更好地出发！😌\n\n" +
                    "试试这几个小方法：\n" +
                    "1. 站起来活动一下，伸个懒腰\n" +
                    "2. 喝杯水，眺望远方\n" +
                    "3. 听一首喜欢的歌\n" +
                    "4. 小憩 10 分钟\n\n" +
                    "休息好了我们再继续！(｡•ᴗ-｡)♡"});

            messages.add(new String[]{"不想学",
                    "每个人都会有不想学习的时候，这很正常~ 💭\n\n" +
                    "试试这些方法找回状态：\n" +
                    "1. 先从最简单的内容开始，降低门槛\n" +
                    "2. 设定一个小目标，比如只学 10 分钟\n" +
                    "3. 想想当初为什么开始学习\n" +
                    "4. 学完后给自己一个小奖励\n\n" +
                    "哪怕只学一点点，也是进步哦！加油 (=^･ω･^=)"});

            messages.add(new String[]{"太难",
                    "遇到困难是正常的，说明你在进步！💎\n\n" +
                    "建议：\n" +
                    "1. 先回顾一下前面的内容，打好基础\n" +
                    "2. 把大问题拆成小问题，逐一攻克\n" +
                    "3. 遇到不懂的地方，可以随时问我哦~\n" +
                    "4. 多看看例子，加深理解\n\n" +
                    "没有学不会的知识，只有还没学会的方法！你一定可以的 (≧∇≦)ﾉ"});

            messages.add(new String[]{"焦虑",
                    "感到焦虑很正常，说明你在乎这件事~ 🤗\n\n" +
                    "试着这样做：\n" +
                    "1. 深呼吸，放轻松\n" +
                    "2. 把担心的事情写下来，看看哪些是真的\n" +
                    "3. 专注于当下，一步一步来\n" +
                    "4. 相信自己，你比想象中更厉害\n\n" +
                    "慢慢来，比较快~ 我会一直陪着你的 (｡•ᴗ-｡)♡"});

            String matchedMsg = null;
            if (mood != null) {
                for (String[] pair : messages) {
                    if (mood.contains(pair[0])) {
                        matchedMsg = pair[1];
                        break;
                    }
                }
            }

            if (matchedMsg == null) {
                result.append("无论遇到什么困难，记住：\n\n");
                result.append("🌟 每一次努力都不会白费\n");
                result.append("🌟 每一次坚持都在让你变得更好\n");
                result.append("🌟 每一次学习都是投资自己\n\n");
                result.append("你已经很棒了！继续往前走，光明就在前方！✨");
            } else {
                result.append(matchedMsg);
            }

            return result.toString();
        } catch (Exception e) {
            log.error("获取激励失败", e);
            return "获取激励时发生错误：" + e.getMessage();
        }
    }
}
