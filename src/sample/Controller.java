package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Controller {

    public static final String NEYMANA = "Neymana:";
    public static final String ANALITICHESK = "Analitichesk:";
    public static final String METROPOLISA = "Metropolisa:";
    public static final String GISTOGRAM = "Gistogram:";
    public static final String NBD_DATA_FOLDER = "/nbdDataFolder/";
    public static final String INVERSE = "Inverse";
    public volatile Boolean isRun = true;
    public boolean isStop = false;
    public Map<Integer,Double> metroData;
    public Map<Integer,Double> neymanaData;
    public Map<Integer,Double> inverseData;

    public void on() {
        //StaticFunction.getNegativBinomialRandom();
    }
    @FXML
    private TextField N_test;

    @FXML
    private TextField A_text;

    @FXML
    private TextField B_text;

    @FXML
    public AnchorPane ancorePane;
    @FXML
    public volatile ProgressBar densityBar;

    @FXML
    public volatile ProgressBar neymanaBar;
    @FXML
    public volatile ProgressBar inverseBar;

    @FXML
    public volatile ProgressBar metropolisaBar;
    @FXML
    private Text text;
    @FXML
    public volatile ProgressBar gistogramBar;

    @FXML
    private TextField p;

    @FXML
    private Button pause;
    @FXML
    private TextField statistic;

    @FXML
    private TextField r;
    public static final Random random = new Random();
    @FXML
    private Button stop;
    @FXML
    private Button start;
    @FXML
    private BarChart<String, Number> chart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;
    private int a = 0;
    private int b = 100;
    Map<Integer, Double> densityMap = new HashMap<>();
    List<Point> neymanaList = new ArrayList<>();
    Map<Integer, Double> gistogramMap = new HashMap<>();
    List<Point> metropolisaList = new ArrayList<>();
    List<Point> inverseList = new ArrayList<>();


    @FXML
    void onPause(MouseEvent event) {
        start.setDisable(false);
        pause.setDisable(true);
    }

    boolean isFirstTimeStart = true;
    boolean isValidateData = true;
    int R;
    double pr;
    int stat;
    double W;
    int N=25;


    @FXML
    void onClick(MouseEvent event) {
        isStop=false;
        if (isValidateData) {
            try{
            R = Integer.parseInt(r.getText());
            if(R<1)throw new RuntimeException();
            pr = Double.parseDouble(p.getText());
            if(pr<=0.0||pr>=1.0)throw new RuntimeException();
            stat = Integer.parseInt(statistic.getText());
            if(stat<1)throw new RuntimeException();
            a= Integer.parseInt(A_text.getText());
            b=Integer.parseInt(B_text.getText());
            }catch (Exception e){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Validation");
                alert.setHeaderText("Statistic - should be int from 1"+System.lineSeparator()+
                        "P - should be from 0 to 1"+System.lineSeparator()+
                        "Sb - should be int from 1");
                alert.show();
                return;
            }
            isValidateData = false;
            W = StaticFunction.countW(R, pr, a, b);
            statistic.setDisable(true);
            p.setDisable(true);
            r.setDisable(true);
        }
        if (start.getText().equals("Start")) {
            start.setText("Pause");
            if (isFirstTimeStart) {
                isRun = true;
                //build density chart
                setText("");
                new Thread(() -> new StaticFunction().methodMetropolisa(a,b, R, pr, stat, this,N)).start();
                new Thread(() -> new StaticFunction().methodInverseCDF(R, pr, stat,N,this)).start();
                new Thread(() -> new StaticFunction().densityAndCDF(a, b, R, pr, W, this)).start();
               // new Thread(() -> new StaticFunction().methodGistogram(a, b, R, pr, this)).start();

                //neyman method
                new Thread(() -> new StaticFunction().neymanMethod(stat, a, b, R, W, pr, this,N)).start();
                isFirstTimeStart=false;
                new Thread(()->{
                   while (true){
                       synchronized (this){
                      if(neymanaBar.getProgress()==1.0&&
                              metropolisaBar.getProgress()==1.0&&
                              inverseBar.getProgress()==1.0){
                          Platform.runLater(()->{
//                              isValidateData = true;
//                              isFirstTimeStart = true;
//                              statistic.setDisable(false);
//                              p.setDisable(false);
//                              r.setDisable(false);
                              isStop = true;
                              stop.setText("Repeat");
                              start.setText("Start");
                          });
                          return;
                      }
                       }
                   }
                }).start();
            } else {
                isRun = true;

            }
        } else {
            start.setText("Start");
            isRun = false;

        }



    }

    @FXML
    void onStop(MouseEvent event) {
        stop.setText("Stop");
        isValidateData = true;
        isFirstTimeStart = true;
        statistic.setDisable(false);
        p.setDisable(false);
        r.setDisable(false);
        isStop = true;
        start.setText("Start");
        neymanaBar.setProgress(0.0);
        metropolisaBar.setProgress(0.0);
        inverseBar.setProgress(0.0);
        densityBar.setProgress(0.0);

    }

    @FXML
    void onColapse(MouseEvent event) {
        Platform.runLater(() ->
                buildHistogramChart(new Stage(),
                        Arrays.asList(getNeymana(),getInverse(),getMetropolisa()),"Together"));

    }



    public void buildChartArea(List<Point> dataXY, String title) {
        AreaChart<String, Number> chart = new AreaChart<String, Number>(new CategoryAxis(), new NumberAxis());
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Point p : dataXY) {
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(String.valueOf(p.getX()).substring(0, 4), p.getY()));
        }
        chart.getData().clear();
        Scene scene = new Scene(chart, 800, 600);
        chart.getData().add(series);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    public void buildChartHistogram(Map<? extends Number, ? extends Number> dataXY, String title) {
        BarChart<String, Number> chart = new BarChart<String, Number>(new CategoryAxis(), new NumberAxis());
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<? extends Number, ? extends Number> entry : dataXY.entrySet()) {
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(String.valueOf(entry.getKey()), entry.getValue()));
        }
        chart.getData().clear();
        Scene scene = new Scene(chart, 800, 600);
        chart.getData().add(series);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    public void buildChartPoint(List<Point> dataXY, String title) {
        Stage stage = new Stage();
        stage.setTitle("Scatter Chart Sample");
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
        Scene scene = new Scene(sc, 800, 600);
        sc.getData().add(series);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void ImportClick(ActionEvent event) {
        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.setTitle("FileUpload");
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            File file = fileChooser.showOpenDialog(stage);
            System.out.println(file.toString());
            fileOnChart(file);
        });
    }

    private void fileOnChart(File file) {
        try {
            XYChart.Series series1 = new XYChart.Series();
            XYChart.Series series2 = new XYChart.Series();
            XYChart.Series series3 = new XYChart.Series();
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            String line = reader.readLine();
            while (line != null) {
                if (line.equals(NEYMANA)) {
                    series1.setName("Neymana");
                    neymanaData = new HashMap<>();
                    line = reader.readLine();
                    while (!line.equals("")) {
                        List<String> lineList = Arrays.asList(line.split("\\s+"));
                        Point p = new Point(Integer.parseInt(lineList.get(0)), Double.parseDouble(lineList.get(1)));
                        neymanaData.put((int)p.getX(),p.getY());
                        series1.getData().add(new XYChart.Data<>(String.valueOf(p.getX()),p.getY()));
                        line = reader.readLine();
                    }
                }
                if (line.equals(METROPOLISA)) {
                    series2.setName("Metropolisa");
                    metroData = new HashMap<>();
                    line = reader.readLine();
                    while (!line.equals("")) {
                        List<String> lineList = Arrays.asList(line.split("\\s+"));
                        Point p = new Point(Integer.parseInt(lineList.get(0)), Double.parseDouble(lineList.get(1)));
                       metroData.put((int)p.getX(),p.getY());
                        series2.getData().add(new XYChart.Data<>(String.valueOf(p.getX()),p.getY()));
                        line = reader.readLine();
                    }
                }

                if (line.equals(INVERSE)) {
                   inverseData=new HashMap<>();
                    line = reader.readLine();
                    series3.setName("Inverse");
                    while (!line.equals("")) {
                        List<String> lineList = Arrays.asList(line.split("\\s+"));
                        Point p = new Point(Integer.parseInt(lineList.get(0)), Double.parseDouble(lineList.get(1)));
                        inverseData.put((int)p.getX(), p.getY());
                        series3.getData().add(new XYChart.Data<>(String.valueOf(p.getX()),p.getY()));
                        line = reader.readLine();
                    }
                }
                line = reader.readLine();
            }
            Platform.runLater(() ->
                    buildHistogramChart(new Stage(),
                            Arrays.asList(series1,series2,series3),"Together"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    void clickAbout(ActionEvent event) {
        Platform.runLater(() -> {
            Parent root = null;
            try {
                root = FXMLLoader.load(getClass().getResource("sample1.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Stage stage = new Stage();
            stage.setTitle("About");
            stage.setScene(new Scene(root, 770, 840));
            stage.show();
//            Alert alert = new Alert(Alert.AlertType.INFORMATION);
//            alert.setTitle("Information");
//            Hyperlink link = new Hyperlink();
//            link.setText("https://en.wikipedia.org/wiki/Negative_binomial_distribution");
//            link.setOnAction(t -> new Main().getHostServices().showDocument(link.getText()));
//            alert.getDialogPane().setContent(link);
//            alert.setHeaderText("This is unreal application for Negative Binomial Distribution. " +
//                    "For more information follow ");
//            alert.show();
        });
    }


    @FXML
    void saveClick(ActionEvent event) {
        Platform.runLater(() -> {
            String fileName = getFileNameFromForm("Save File", "fileExample.txt");
            saveDataToFile(fileName);
        });
    }

    private String getDataText() {
        StringBuilder sb = new StringBuilder();
        sb.append(NEYMANA).append(System.lineSeparator());
        for (Map.Entry<Integer,Double> p : neymanaData.entrySet()) {
            sb.append(p.getKey()).append("    ").append(p.getValue()).append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        sb.append(INVERSE).append(System.lineSeparator());
        for (Map.Entry<Integer,Double> p : inverseData.entrySet()) {
            sb.append(p.getKey()).append("    ").append(p.getValue()).append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        sb.append(METROPOLISA).append(System.lineSeparator());
        for (Map.Entry<Integer,Double> p : neymanaData.entrySet()) {
            sb.append(p.getKey()).append("    ").append(p.getValue()).append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        return sb.toString();
    }


    private void saveDataToFile(String fileName) {
        FileWriter writer = null;
        try {
            String mainPath = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
            new File(mainPath + NBD_DATA_FOLDER).mkdirs();
            File myObj = new File(mainPath + NBD_DATA_FOLDER + fileName);
            writer = new FileWriter(myObj, false);
            String text = getDataText();
            writer.write(text);
            writer.flush();
        } catch (IOException ex) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(ex.getMessage());
                alert.show();
            });
        } finally {
            try {
                assert writer != null;
                writer.close();
            } catch (IOException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText(e.getMessage());
                    alert.show();
                });
            }
        }
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("File information");
            alert.setTitle("Information");
            alert.setContentText("File save successful" + System.lineSeparator() + "Find it in Documents in " + NBD_DATA_FOLDER);
            alert.show();
        });
    }
    private XYChart.Series neymana;
    private XYChart.Series inverse;
    private XYChart.Series metropolisa;

    private String getFileNameFromForm(String header, String defaultValue) {
        TextInputDialog td = new TextInputDialog(defaultValue);
        td.setContentText("File Path");
        td.setHeaderText(header);
        td.setTitle("File windows");
        String res = null;
        while (Objects.isNull(res)) {
            Optional<String> result = td.showAndWait();
            if (result.isPresent()) {
                res = result.get();
            } else {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("Please input valid file");
                    alert.show();
                });
            }
        }
        return res;
    }
    public static void buildHistogramChart(Stage stage, List<XYChart.Series> series, String title) {
        BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        chart.getData().clear();
        Scene scene = new Scene(chart, 550, 350);
        for (XYChart.Series s : series) {
            chart.getData().add(s);
        }
        chart.setTitle(title);

        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    public XYChart.Series getNeymana() {
        return neymana;
    }

    public void setNeymana(XYChart.Series neymana) {
        this.neymana = neymana;
    }

    public XYChart.Series getInverse() {
        return inverse;
    }

    public void setInverse(XYChart.Series inverse) {
        this.inverse = inverse;
    }

    public XYChart.Series getMetropolisa() {
        return metropolisa;
    }

    public void setMetropolisa(XYChart.Series metropolisa) {
        this.metropolisa = metropolisa;
    }

    public Text getText() {
        return text;
    }

    public void setText(String text) {
        this.text.setText(text);
    }
}
//        dataXY2=dataXY2.entrySet().stream().sorted(Comparator.comparingDouble(Map.Entry::getValue)).
//                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
//                (e1, e2) -> e1, LinkedHashMap::new));

