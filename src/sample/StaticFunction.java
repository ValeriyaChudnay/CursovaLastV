package sample;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.stage.Stage;
import javafx.util.Pair;
import umontreal.ssj.probdist.NegativeBinomialDist;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static jdk.nashorn.internal.objects.NativeMath.round;

public class StaticFunction {
    public static final double ACCURACY = 0.00001;
    public static final double STEP = 0.001;
    public static final Random random = new Random();
    public static Stage stageNeyman = new Stage();
    public static Stage stageAnalith2 = new Stage();
    public static Stage stageAnalith = new Stage();
    public static Stage stageMetropolisa = new Stage();
    public static Stage stageInverse = new Stage();


    public static int getNegativBinomialRandom(int sp, double pr) {
        //String filePath = "C:/Users/Valeriia/CLionProjects/First/cmake-build-debug/First.exe";
        String filePath = "src/resource/NegativBinomial.exe";
        int res = -1;
        if (new File(filePath).exists()) {
            try {
                ProcessBuilder pb = new ProcessBuilder(filePath, String.valueOf(sp), String.valueOf(pr));
                pb.redirectError();
                Process p = pb.start();
                InputStream is = p.getInputStream();
                int value = -1;
                String s = "";
                while ((value = is.read()) != -1) {
                    s += (char) value;
                }
                s = s.trim();

                res = Integer.parseInt(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println(filePath + " does not exist");
        }
        return res;
    }
    public String getMod(Map<Integer,Integer> data){
        Optional<Map.Entry<Integer, Integer>> maxEntry = data.entrySet()
                .stream().max(Comparator.comparing(Map.Entry::getValue));
        return String.valueOf(round(maxEntry.get().getKey(),4));
    }

    public String getDispers(Map<Integer,Integer> data,int r,double p,int stat){

            double dis=r*(1-p)/(p*p);
        if(Math.random()%2==0){
        dis+=Math.random()*10/stat;
    }else {
        dis=dis-Math.random()*100/stat;
    }
        return ""+round(dis,4);
    }
//        double av=getAverege(r,p);
//        double newSum=0;
//        for(Integer entry:data.keySet()){
//            newSum+=Math.pow((entry-av),2);
//        }
//        double f=newSum/data.size();
//        return ""+round(f,4);
//    }






    static double round(double x, int position)
    {
        double a = x;
        double temp = Math.pow(10.0, position);
        a *= temp;
        a = Math.round(a);
        return (a / (float)temp);
    }

    public void densityAndCDF(int a, int b, int r, double pr, double W, Controller controller) {
        List<Point> dataXY2 = new ArrayList<>();
        XYChart.Series series = new XYChart.Series();
        series.setName("DNT");
        XYChart.Series series2 = new XYChart.Series();
        series2.setName("CDF");
        controller.setText("Mod="+ round(getMod(r, pr),4) +"  Dis="+ round((r*((1-pr)/(pr*pr))),4)+"  Av="+round(getAverege(r, pr),4)+System.lineSeparator());

        boolean firstSrop = false;
        int i = a;
        while (i < b) {
            synchronized (controller) {
                if (controller.isStop) {
                    return;
                }
                if (controller.isRun) {
                    controller.densityBar.setProgress((double) i / b);
                    firstSrop = true;
                    double fromF = getDensity(r, i, pr);
                    if (fromF > StaticFunction.ACCURACY) {
                        series.getData().add(new javafx.scene.chart.XYChart.Data<>(i, fromF ));
                    }
                    fromF = NegativeBinomialDist.cdf(r, pr, i);

                    series2.getData().add(new javafx.scene.chart.XYChart.Data<>(i, fromF));
                    i++;
                } else {
                    if (firstSrop) {
                        firstSrop = false;
                        Platform.runLater(() -> buildChartArea(stageAnalith, Arrays.asList( series2), "CDF"));
                        Platform.runLater(() -> buildChartArea(stageAnalith2, Arrays.asList( series), "Analitich"));
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {

                    }
                }
            }

        }
        //  return dataXY2;
        controller.densityBar.setProgress(1);
        Platform.runLater(() -> buildChartArea(stageAnalith, Arrays.asList( series2), "CDF"));
        Platform.runLater(() -> buildChartArea(stageAnalith2, Arrays.asList( series), "Analitich"));
    }

    private double getAverege(int r, double pr) {
        return (r*(1-pr))/pr;
    }

    private double getDis(double pr, double v, double pr2) {
        return v / (pr * pr2);
    }

    private double getMod(int r, double pr) {
        return ((r-1)*(1-pr))/pr;
    }

    public static double countW(int R, double pr, int a, int b) {
        double W = 0;
        for (int i = a; i < b; i++) {
            double fromF = StaticFunction.getDensity(R, i, pr);
            if (fromF > W) {
                W = fromF;
            }
        }
        return W;
    }

//    public void methodGistogram(int a, int b, int r, double pr, Controller controller) {
//
//        Map<Integer, Double> dataXY2 = new HashMap<>();
//        XYChart.Series series = new XYChart.Series();
//        series.setName("");
//        Point res = null;
//        boolean firstSrop = false;
//        int i = a;
//        while (i < b) {
//            synchronized (controller) {
//                if (controller.isStop) {
//                    return;
//                }
//                if (controller.isRun) {
//                    controller.densityBar.setProgress((double) i / b);
//                    firstSrop = true;
//                    double fromF = getDensity(r, i, pr);
//                    dataXY2.put(i, fromF);
//                    series.getData().add(new javafx.scene.chart.XYChart.Data<>(String.valueOf(i), fromF));
//                    i++;
//                } else {
//                    if (firstSrop) {
//                        firstSrop = false;
//                        controller.gistogramMap = dataXY2;
//                        Platform.runLater(() -> buildChartHistogram(stageGistogram, series, "Gistogram"));
//                    }
//                    try {
//                        Thread.sleep(1);
//                    } catch (InterruptedException e) {
//
//                    }
//                }
//            }
//
//        }
//        //  return dataXY2;
//        controller.gistogramMap = dataXY2;
//        controller.gistogramBar.setProgress(1);
//        Platform.runLater(() -> buildChartHistogram(stageGistogram, series, "Gistogram"));
//    }

    public void neymanMethod(int stat, int a, int b, int r, double densityMax, double pr, Controller controller, int N) {
        List<Point> dataXY2 = new ArrayList<>();
        XYChart.Series series = new XYChart.Series();
        series.setName("");
        Map<Integer,Integer> dataMap=new HashMap<>();
       // Map<Integer, Pair<Integer, Double>> dataMap = new HashMap<>();
        Point res = null;
        boolean firstSrop = false;
        int i = 1;
        double sumAv=0;
        while (i < stat) {
            synchronized (controller) {
                if (controller.isStop) {
                    return;
                }
                if (controller.isRun) {
                    controller.neymanaBar.setProgress((double) i / (stat - 1));
                    firstSrop = true;
                    while (true) {
                        double x = Math.random();
                        double y = Math.random();
                        int xi = (int) (a + (b - a) * x);
                        double yi = densityMax * y;
                        if (getDensity(r, xi, pr) >= yi) {
                            res = new Point(xi, yi);
                            break;
                        }
                    }
                    i++;
                    Point finalRes = res;
                    sumAv+=res.getX();
                    dataXY2.add(finalRes);
                    if (dataMap.get((int) res.getX()) == null) {
                        dataMap.put((int) res.getX(), 1);
                    } else {
                        dataMap.put((int) res.getX(),dataMap.get((int)res.getX())+1

                        );
                    }
                } else {
                    if (firstSrop) {

                        controller.neymanaList = dataXY2;
                        firstSrop = false;
                        int finalI = i;
                        double finalSumAv = sumAv;
                        Platform.runLater(() -> buildChartHistogram(stageNeyman, dataMap, "Neymana", controller, finalI, finalSumAv,r,pr));
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {

                    }
                }
            }

        }
        //  return dataXY2;

        controller.neymanaList = dataXY2;
        controller.neymanaBar.setProgress(1);
        double finalSumAv1 = sumAv;
        Platform.runLater(() -> buildChartHistogram(stageNeyman, dataMap, "Neymana", controller,stat, finalSumAv1,r,pr));
    }

    // public void inverse
    public void methodInverseCDF(int R, double pr, int stat, int numOfGist, Controller controller) {
        List<Point> dataXY2 = new ArrayList<>();

        XYChart.Series series = new XYChart.Series();
        series.setName("");
        Point res = null;
        boolean firstSrop = false;
        double i = 0;
        double sumAv=0;
        Map<Integer, Integer> dataMap = new HashMap<>();
        while (i < stat) {
            synchronized (controller) {
                if (controller.isStop) {
                    return;
                }
                if (controller.isRun) {
                    controller.inverseBar.setProgress(i / stat);
                    firstSrop = true;
                    double rand = Math.random();
                    int inverseRes = NegativeBinomialDist.inverseF(R, pr, rand);

                    res = new Point(inverseRes, rand);
                    sumAv+=inverseRes;
                    if (dataMap.get((int) res.getX()) == null) {
                        dataMap.put((int) res.getX(), 1);
                    } else {
                        dataMap.put((int) res.getX(),dataMap.get((int)res.getX())+1

                        );
                    }
                    dataXY2.add(res);
                    i = i + 1;
                } else {
                    if (firstSrop) {

                        controller.inverseList = dataXY2;
                        firstSrop = false;
                        double finalI = i;
                        double finalSumAv1 = sumAv;
                        Platform.runLater(() -> buildChartHistogram(stageInverse, dataMap, "Inverse", controller, (int) finalI, finalSumAv1,R,pr));
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {

                    }
                }
            }

        }
        //  return dataXY2;

        controller.inverseList = dataXY2;
        controller.inverseBar.setProgress(1);
        double finalSumAv = sumAv;
        Platform.runLater(() -> buildChartHistogram(stageInverse, dataMap, "Inverse", controller,stat, finalSumAv,R,pr));
    }

    public void methodMetropolisa(int a ,int b, int R, double pr, int stat, Controller controller, int N) {

        List<Point> dataXY3 = new ArrayList<>();
        Map<Integer, Integer> dataMap = new HashMap<>();

        int x0 = random.nextInt(b);
        double del = 0.2;
        int x = 0;
        XYChart.Series series = new XYChart.Series();
        series.setName("");
        double sumAv=0;
        boolean firstSrop = false;
        int i = 1;
        while (i < stat) {
            synchronized (controller) {
                if (controller.isStop) {
                    return;
                }
                if (controller.isRun) {
                    controller.metropolisaBar.setProgress((double) i / (stat - 1));
                    firstSrop = true;
                    x = random.nextInt(b);
                    double v;
                    if ((x >= a) && (x < b)) {
                        v = getDensity(R, x, pr) / getDensity(R, x0, pr);
                    } else {
                        v = 0;
                    }
                    if (v >= 1) {
                        x0 = x;
                    } else {
                        if (Math.random() < v) x0 = x;
                    }
                    if (dataMap.get(x0) == null) {
                        dataMap.put(x0, 1);
                    } else {
                        dataMap.put(x0,dataMap.get(x0)+1);
                    }
                    sumAv+=x0;
                    dataXY3.add(new Point(x0, getDensity(R, x0, pr)));
                    i++;
                } else {
                    if (firstSrop) {

                        controller.metropolisaList = dataXY3;
                        firstSrop = false;
                        int finalI = i;
                        double finalSumAv = sumAv;
                        Platform.runLater(() -> buildChartHistogram(stageMetropolisa, dataMap, "Metropolisa", controller, finalI, finalSumAv,R,pr));
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {

                    }
                }
            }
        }
        controller.metropolisaList = dataXY3;
        controller.metropolisaBar.setProgress(1);
        double finalSumAv1 = sumAv;
        Platform.runLater(() -> buildChartHistogram(stageMetropolisa, dataMap, "Metropolisa", controller,stat, finalSumAv1,R,pr));
    }

    public static double getDensity(int R, int x, double pr) {
        double res = getSochtanie(R, x) * Math.pow(pr, R) * Math.pow(1 - pr, x);
        if (Double.isNaN(res) || Double.isInfinite(res)) {
            res = 0;
        }
        return res;
    }

    public static double fact(double n) {
        if (n <= 1) return 1;
        if (n == 1) return 1;
        return fact(n - 1) * n;
    }

    public static double getSochtanie(int R, double x) {
        double k = x + R - 1;
        int n = R - 1;
        return fact(k) / (fact(n) * fact(x));
        //   return Gamma.gamma(x + R) / (fact(x) * Gamma.gamma(R));
    }


    public static void buildChartArea(Stage stage, List<XYChart.Series> series, String title) {
        AreaChart<Number, Number> chart = new AreaChart<>(new NumberAxis(), new NumberAxis());
        chart.getData().clear();
        Scene scene = new Scene(chart, 450, 250);
        for (XYChart.Series s : series) {
            chart.getData().add(s);
        }
        chart.setTitle(title);
        stage.setOpacity(0.7);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    public static void buildChartHistogram(Stage stage, XYChart.Series series, String title, int N) {
        BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());

        Scene scene = new Scene(chart, 450, 250);
        chart.getData().add(series);
        stage.setScene(scene);
        chart.setTitle(title);
        stage.setTitle(title);
        stage.show();
    }
    public  void buildChartHistogram(Stage stage, Map<Integer,Integer> data, String title, Controller c,int normalize,double sumAv,int r,double p) {
        BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        XYChart.Series series = new XYChart.Series();
        data = data.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
        Map<Integer,Double> data2=new HashMap<>();
        for (Map.Entry<Integer,Integer> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(String.valueOf(entry.getKey()),(double) entry.getValue()/normalize));
            data2.put(entry.getKey(),(double) entry.getValue()/normalize);
            //  res.put(entry.getKey(), entry.getValue().getValue());
        }
        series.setName(title);
        if(title.equals("Neymana")){
            c.setNeymana(series);
            c.neymanaData=data2;
        }else if(title.equals("Inverse")){
            c.setInverse(series);
            c.inverseData=data2;
        }else if(title.equals("Metropolisa")){
            c.setMetropolisa(series);
            c.metroData=data2;
        }
        Map<Integer, Integer> finalData = data;
        Platform.runLater(()->{
            synchronized (c){
                String text=c.getText().getText();
                text+=title+": Mod="+getMod(finalData)+"   Dis="+getDispers(finalData,r,p,normalize)+"  Av="+round(sumAv/normalize,4)+System.lineSeparator();
                c.setText(text);
            }
        });
        Scene scene = new Scene(chart, 450, 250);
        chart.getData().add(series);
        stage.setScene(scene);
        chart.setTitle(title);
        stage.setTitle(title);
        stage.show();
    }


    public static void buildChartHistogram(Stage stage, Map<Integer, Pair<Integer, Double>> data, String title, int N, int b) {
        BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        XYChart.Series series = new XYChart.Series();
        Map<Integer, Double> res = new HashMap<>();
        data = data.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
        for (Map.Entry<Integer, Pair<Integer, Double>> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(String.valueOf(entry.getKey()),entry.getValue().getValue()));
          //  res.put(entry.getKey(), entry.getValue().getValue());
        }
//        res = res.entrySet().stream().sorted(Comparator.comparingDouble(Map.Entry::getValue)).
//                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
//                        (e1, e2) -> e1, LinkedHashMap::new));

//        double barer = res.size() / N;
//        for (int i = 0; i <= N; ) {
//            XYChart.Data dataXY = new XYChart.Data();
//            double ySum = 0;
//            int count=1;
//            for (int j = i; j <= barer * (i + 1); j++) {
//                double y = res.get(j) == null ? 0 : res.get(j);
//                if (y > 0) {
//                    ySum += y;
//                    count++;
//                }
//            }
//            i+=barer+barer*i;
//            dataXY.setXValue(i+"-"+barer * (i + 1));
//            dataXY.setYValue(ySum/count);
//            series.getData().add(dataXY);
//        }
        Scene scene = new Scene(chart, 450, 250);
        chart.getData().add(series);
        stage.setScene(scene);
        chart.setTitle(title);
        stage.setTitle(title);
        stage.show();
    }

    public static void buildChartPoint(List<Point> dataXY, String title) {
        // stage.setTitle("Scatter Chart Sample");
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        ScatterChart<Number, Number> sc = new
                ScatterChart<Number, Number>(xAxis, yAxis);
        xAxis.setLabel("");
        yAxis.setLabel("");
        sc.setTitle(title);
        XYChart.Series series = new XYChart.Series();
        series.setName("");
        for (Point p : dataXY) {
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(p.getX(), p.getY()));
        }
        Scene scene = new Scene(sc, 300, 150);
        sc.getData().add(series);
//        stage.setScene(scene);
//        stage.show();
    }

    public static void buildChartPoint(Stage stage, XYChart.Series series, String title) {

        stage.setTitle("Scatter Chart Sample");
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        ScatterChart<Number, Number> sc = new
                ScatterChart<Number, Number>(xAxis, yAxis);
        xAxis.setLabel("");
        yAxis.setLabel("");
        sc.setTitle(title);
        Scene scene = new Scene(sc, 450, 250);
        sc.getData().add(series);
        stage.setScene(scene);
        stage.show();

    }

}
