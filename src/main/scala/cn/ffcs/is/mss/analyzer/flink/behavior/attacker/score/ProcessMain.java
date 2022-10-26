package cn.ffcs.is.mss.analyzer.flink.behavior.attacker.score;

import cn.ffcs.is.mss.analyzer.ml.AHP.core.*;
import cn.ffcs.is.mss.analyzer.ml.AHP.method.EigenvectorMethod;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hanyu
 * @ClassName ProcessMain
 * @date 2022/3/21 16:22
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class ProcessMain {
    public static void main(String[] args) {

        String score = ProcessMain.getScore("00001@hy;127.0.0.1|127.0.0.2|127.0.0.3|127.0.0.4|127.0.0.5;1|2|3|4|5;127.0.0.1|127.0.0.2|127.0.0.3|127.0.0.4|127.0.0.5;1|2|3|4|5|6|7;" +
                "1648024743000;1648543143000;0;beijin|shanghai|hangzhou|shangxi|shangh;财富|报表|平台|tai|gou;云网|安管|tai|gou|sad;慢速扫描|未知风险-首次出现|" +
                "未知风险-参数异常|未知风险-特征|无用账户检测;web;UA攻击|目录遍历;爬虫检测|运维人员下载;同一时间多地登录|主动外联|ip互访;未知风险-命令执行|xss|sql|ddos|流量异常");

        System.out.println(score);
    }

    public static String getScore(String args) {
        String[] str = args.split(";", -1);
        DataRecord dataRecord = new DataRecord(str);
        List<Map<String, Object>> maps = changControl(dataRecord);
        Object attackerProblem = maps.get(0).get("attackerProblem");
        Object everyScore = maps.get(0).get("everyScore");
        DecisionProblem problem = (DecisionProblem) attackerProblem;
        double result = getSolverScore(problem);
        String conclusion = String.format("%.2f", result);
        double resultScore = Double.parseDouble(conclusion);
        //detail:6.67|9|7.5|10|8.34
        //score:7.40
//        String consequence = "detail:" + everyScore.toString() + "\n" + "score:" + resultScore + "\n";
        String attackerScore;
        try {
            String[] split = everyScore.toString().split("\\|", -1);
            double score = Double.parseDouble(split[split.length - 1]);
            attackerScore = String.format("%.2f", (resultScore / 10.0D) + score);
            return attackerScore;
        } catch (Exception e) {
//            System.out.println("Source Data Error" + args);
            return "";
        }

    }

    private static double getSolverScore(DecisionProblem problem) {
        Solver solver = new Solver();
        solver.addMethod(new EigenvectorMethod());
        List<ProblemSolution> solutions = solver.solve(problem);
        return solutions.get(0).getWeights()[0];

    }


    private static List<Map<String, Object>> changControl(DataRecord dataRecord) {
        DecisionProblem attackerProblem = new DecisionProblem("Attacker_Assess");
        attackerProblem.addAlternative("Attacker_No1");
        //5*5行列式
        Criterion goal0 = attackerProblem.addCriteria("Address_Info");
        Criterion goal1 = attackerProblem.addCriteria("Time_Info");
        Criterion goal2 = attackerProblem.addCriteria("Identity_Info");
        Criterion goal3 = attackerProblem.addCriteria("Business_Info");
        Criterion goal4 = attackerProblem.addCriteria("Alert_Info");
        //4*4行列式
        Criterion goal00 = goal0.addCriterion("Source_Ip");
        Criterion goal01 = goal0.addCriterion("Source_port");
        Criterion goal02 = goal0.addCriterion("Des_Ip");
        Criterion goal03 = goal0.addCriterion("Des_port");
        //2*2行列式
        Criterion goal10 = goal1.addCriterion("Start_Time");
        Criterion goal11 = goal1.addCriterion("Stop_Time");
        //2*2行列式
        Criterion goal20 = goal2.addCriterion("Organization");
        Criterion goal21 = goal2.addCriterion("Used_Place");
        //2*2行列式
        Criterion goal30 = goal3.addCriterion("Login_Major");
        Criterion goal31 = goal3.addCriterion("Login_System");
        //6*6
        Criterion goal40 = goal4.addCriterion("Detect");
        Criterion goal41 = goal4.addCriterion("Deliver");
        Criterion goal42 = goal4.addCriterion("Infiltrate");
        Criterion goal43 = goal4.addCriterion("BreakThrough");
        Criterion goal44 = goal4.addCriterion("Control");
        Criterion goal45 = goal4.addCriterion("Destroy");
        /**
         *@return
         *@author hanyu
         *@date 2022/3/22 16:13
         *@description
         *     目标形成判断矩阵 采用相对尺度，减少不同性质间的比较困难.
         *@update [no][date YYYY-MM-DD][name][description]
         */
        attackerProblem.setPCMatrix(new PCMatrix(new double[][]{
                {1.00D, 2.00D, 1 / 3D, 1 / 5D, 1 / 8D},
                {1 / 2D, 1.00D, 1 / 3D, 1 / 5D, 1 / 7D},
                {3.00D, 3.00D, 1.00D, 1 / 3D, 1 / 5D},
                {4.00D, 5.00D, 3.00D, 1.00D, 1 / 5D},
                {8.00D, 7.00D, 5.00D, 5.00D, 1.00D}
        }));
        goal0.setPCMatrix(new PCMatrix(new double[][]{
                {1.0D, 1.0D, 1 / 4D, 1 / 3D},
                {1.0D, 1.0D, 1 / 3D, 0.5D},
                {4.0D, 3.0D, 1.0D, 2.0D},
                {3.0D, 2.0D, 0.5D, 1.0D}
        }));
        goal1.setPCMatrix(new PCMatrix(new double[][]{
                {1.0D, 1.0D},
                {1.0D, 1.0D}}));
        goal2.setPCMatrix(new PCMatrix(new double[][]{
                {1.0D, 2.0D},
                {1 / 2D, 1.0D}}));
        goal3.setPCMatrix(new PCMatrix(new double[][]{
                {1.0D, 1.0D},
                {1.0D, 1.0D}}));

        goal4.setPCMatrix(new PCMatrix(new double[][]{
//                {1.0D, 1.0D, 1 / 2D, 1 / 2D, 1 / 3D, 1 / 3D},
//                {1.0D, 1.0D, 1 / 2D, 1 / 2D, 1 / 3D, 1 / 3D},
//                {2.0D, 1.0D, 1.0D, 1.0D, 1 / 2D, 1 / 2D},
//                {2.0D, 2.0D, 1.0D, 1.0D, 1 / 2D, 1 / 2D},
//                {3.0D, 3.0D, 2.0D, 2.0D, 1.0D, 1.0D},
//                {3.0D, 3.0D, 2.0D, 2.0D, 1.0D, 1.0D}}));
                {1.0D, 1 / 2.0D, 1 / 3.0D, 1 / 4.0D, 1 / 5.0D, 1 / 9.0D},
                {2.0D, 1.0D, 1 / 2.0D, 1 / 3.0D, 1 / 4.0D, 1 / 5.0D},
                {3.0D, 2.0D, 1.0D, 1 / 2.0D, 1 / 3.0D, 1 / 4.0D},
                {4.0D, 3.0D, 2.0D, 1.0D, 1 / 2.0D, 1 / 3.0D},
                {5.0D, 4.0D, 3.0D, 2.0D, 1.0D, 1 / 2.0D},
                {6.0D, 5.0D, 4.0D, 3.0D, 2.0D, 1.0D}}));


        double[] address = getScoreByAddressInfo(dataRecord);
        double[] time = getScoreByTimeInfo(dataRecord);
        double[] identity = getScoreByIdentityInfo(dataRecord);
        double[] business = getScoreByBusinessInfo(dataRecord);
        double[] alert = getScoreByAlertInfo(dataRecord);
        goal00.setPCMatrix(new PCMatrix(new double[][]{{address[0]}}));
        goal01.setPCMatrix(new PCMatrix(new double[][]{{address[1]}}));
        goal02.setPCMatrix(new PCMatrix(new double[][]{{address[2]}}));
        goal03.setPCMatrix(new PCMatrix(new double[][]{{address[3]}}));

        goal10.setPCMatrix(new PCMatrix(new double[][]{{time[0]}}));
        goal11.setPCMatrix(new PCMatrix(new double[][]{{time[1]}}));

        goal20.setPCMatrix(new PCMatrix(new double[][]{{identity[0]}}));
        goal21.setPCMatrix(new PCMatrix(new double[][]{{identity[1]}}));

        goal30.setPCMatrix(new PCMatrix(new double[][]{{business[0]}}));
        goal31.setPCMatrix(new PCMatrix(new double[][]{{business[1]}}));

        goal40.setPCMatrix(new PCMatrix(new double[][]{{alert[0]}}));
        goal41.setPCMatrix(new PCMatrix(new double[][]{{alert[1]}}));
        goal42.setPCMatrix(new PCMatrix(new double[][]{{alert[2]}}));
        goal43.setPCMatrix(new PCMatrix(new double[][]{{alert[3]}}));
        goal44.setPCMatrix(new PCMatrix(new double[][]{{alert[4]}}));
        goal45.setPCMatrix(new PCMatrix(new double[][]{{alert[5]}}));

        double addressScore = (address[0] + address[1] + address[2] + address[3]) / alert.length;
        double timeScore = (time[0] + time[1]) / time.length;
        double identityScore = (identity[0] + identity[1]) / identity.length;
        double businessScore = (business[0] + business[1]) / business.length;
        double alertScore = (alert[0] + alert[1] + alert[2] + alert[3] + alert[4] + alert[5]) / alert.length;


        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setRoundingMode(RoundingMode.UP);
        String result1 = numberFormat.format(addressScore);
        String result2 = numberFormat.format(timeScore);
        String result3 = numberFormat.format(identityScore);
        String result4 = numberFormat.format(businessScore);
        String result5 = numberFormat.format(alertScore);
        String allScore = result1 + "|" + result2 + "|" + result3 + "|" + result4 + "|" + result5;
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        stringObjectHashMap.put("attackerProblem", attackerProblem);
        stringObjectHashMap.put("everyScore", allScore);
        list.add(stringObjectHashMap);

        return list;


    }

    private static double[] getScoreByAlertInfo(DataRecord dataRecord) {
        double[] res = {0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D};
        String detect = dataRecord.getAlertNameDetect() == null ? "" : dataRecord.getAlertNameDetect();
        if (detect.length() >= 5) {
            res[0] = 10.0D;
        } else if (detect.length() == 4) {
            res[0] = 9.0D;
        } else if (detect.length() == 3) {
            res[0] = 8.0D;
        } else if (detect.length() == 2) {
            res[0] = 7.0D;
        } else if (detect.length() == 1) {
            res[0] = 6.0D;
        } else {
            res[0] = 5.0D;
        }
        String deliver = dataRecord.getAlertNameDeliver() == null ? "" : dataRecord.getAlertNameDeliver();
        if (deliver.length() >= 1) {
            res[1] = 10.0D;
        } else {
            res[1] = 5.0D;
        }
        String infiltrate = dataRecord.getAlertNameInfiltrate() == null ? "" : dataRecord.getAlertNameInfiltrate();
        if (infiltrate.length() >= 2) {
            res[2] = 10.0D;
        } else if (infiltrate.length() == 1) {
            res[2] = 9.0D;
        } else {
            res[2] = 5.0D;
        }
        String breakThrough = dataRecord.getAlertNameBreakThrough() == null ? "" : dataRecord.getAlertNameBreakThrough();
        if (breakThrough.length() >= 2) {
            res[3] = 10.0D;
        } else if (breakThrough.length() == 1) {
            res[3] = 9.0D;
        } else {
            res[3] = 5.0D;
        }
        String control = dataRecord.getAlertNameControl() == null ? "" : dataRecord.getAlertNameControl();
        if (control.length() > 0) {
            res[3] = 10.0D;
        } else {
            res[3] = 5.0D;
        }
        String destroy = dataRecord.getAlertNameDestroy() == null ? "" : dataRecord.getAlertNameDestroy();
        if (destroy.length() > 0) {
            res[4] = 10.0D;
        } else {
            res[4] = 5.0D;
        }
        return res;
    }

    private static double[] getScoreByBusinessInfo(DataRecord dataRecord) {
        double[] res = {0.0D, 0.0D};
        String loginMajor = dataRecord.getAttackLoginMajor() == null ? "" : dataRecord.getAttackLoginMajor();
        res[0] = getStringValueScore(loginMajor);
        String loginSystem = dataRecord.getAttackLoginSystem() == null ? "" : dataRecord.getAttackLoginSystem();
        res[1] = getStringValueScore(loginSystem);
        return res;

    }

    private static double[] getScoreByIdentityInfo(DataRecord dataRecord) {
        double[] res = {0.0D, 0.0D};
        String organization = dataRecord.getAttackOrganization() == null ? "" : dataRecord.getAttackOrganization();
        if (organization.contains("1")) {
            res[0] = 10.0D;
        } else {
            res[0] = 5.0D;
        }
        String usedPlace = dataRecord.getAttackUsedPlace() == null ? "" : dataRecord.getAttackUsedPlace();
        res[1] = getStringValueScore(usedPlace);
        return res;
    }

    private static double[] getScoreByTimeInfo(DataRecord dataRecord) {
        double[] res = {0.0D, 0.0D};
        long startTime = dataRecord.getAttackStartTime() == null ? 0L : dataRecord.getAttackStartTime();
        long stopTime = dataRecord.getAttackStopTime() == null ? 0L : dataRecord.getAttackStopTime();
        if (startTime < stopTime) {
            res[0] = 10.0D;
        } else {
            res[0] = 5.0D;
        }
        double longValueScore = getLongValueScore(startTime, stopTime);
        res[1] = longValueScore;

        return res;
    }

    private static double[] getScoreByAddressInfo(DataRecord dataRecord) {
        double[] res = {0.0D, 0.0D, 0.0D, 0.0D};
        String sourIp = dataRecord.getAttackSourceIp() == null ? "" : dataRecord.getAttackSourceIp();
        res[0] = getStringValueScore(sourIp);
        String sourcePort = dataRecord.getAttackSourcePort() == null ? "" : dataRecord.getAttackSourcePort();
        res[1] = getStringValueScore(sourcePort);
        String desIp = dataRecord.getAttackDesIp() == null ? "" : dataRecord.getAttackDesIp();
        res[2] = getStringValueScore(desIp);
        String desPort = dataRecord.getAttackDesPort() == null ? "" : dataRecord.getAttackDesPort();
        res[3] = getStringValueScore(desPort);
        return res;
    }

    private static double getStringValueScore(String string) {
        String valueStr = string == null ? "" : string;
        int length = valueStr.split("\\|", -1).length;
        double score;
        if (length >= 5) {
            score = 10.0D;
        } else {
            score = length * 2;
        }
        return score;
    }

    private static double getLongValueScore(Long start, Long stop) {
        long startTime = start == null ? 0L : start;
        long stopTime = stop == null ? 0L : stop;
        int day;
        double score;
        if (stopTime > startTime && stopTime != 0L & startTime != 0L) {
            day = (int) (stopTime - startTime) / (1000 * 3600 * 24);
        } else {
            day = 0;
        }
        if (day >= 7) {
            score = 9.0D;
        } else if (day == 6) {
            score = 8.0D;
        } else if (day == 5) {
            score = 7.0D;
        } else if (day == 4) {
            score = 6.0D;
        } else if (day == 3) {
            score = 5.0D;
        } else if (day == 2) {
            score = 4.0D;
        } else if (day == 1) {
            score = 3.0D;
        } else {
            score = 2.0D;
        }
        return score;


    }


}
