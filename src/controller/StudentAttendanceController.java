package controller;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import jssc.*;
import model.ConnectionHandler;

import java.io.*;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;


public class StudentAttendanceController implements Initializable {

    @FXML
    private Circle studimageCircle;

    @FXML
    private Label dateLbl;

    @FXML
    private JFXTextField studrfidTxt;

    @FXML
    private JFXTextField studidTxt;

    @FXML
    private JFXTextField studyrTxt;

    @FXML
    private JFXTextField studsecTxt;

    @FXML
    private JFXTextField studdeptTxt;

    @FXML
    private JFXTextField studnameTxt;

    @FXML
    private Label constatusLbl;

    @FXML
    private Label portconnectedLbl;

    @FXML
    private Label dbstatusLbl;

    @FXML
    private JFXButton configBtn;

    @FXML
    private JFXButton connectBtn;

    // declare var below
    private DatabaseAccessObject dao;
    private String query;
    private static StudentAttendanceController instance;
    private String currentTime,currentDate;
    private Date date = new Date();

    // for rfid
    static ConnectionHandler connector = new ConnectionHandler();
    static SerialPort serialPort = new SerialPort("COM3");
    static Thread threadToInterrupt = null;
    static InputStream inputStream = null;
    private Image image;
    public String rfidport = "",gsmport = "";


    // end of var below

    // init itself
    public StudentAttendanceController() {
        this.instance = this;
    }

    public static StudentAttendanceController getStudentAttendanceController() {
        return instance;
    }
    // end of init itself

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initClock();
        initFields();

        // initialize class
        dao = new DatabaseAccessObject();
        // end of initialize class

        // methods

        // end of methods

        // event buttons
        connectBtn.setOnAction(event -> {
            if(serialPort.isOpened()){
                connectBtn.setText("Disconnect");
                try {
                    serialPort.closePort();
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }else{
                connectBtn.setText("Connect");
                try {
                    initRfid();
                    initFields();
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }

        });
        configBtn.setOnAction(event -> {
            try {
                createPage(null,"/view/ConfigPage.fxml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        

        // end of event buttons
    }

    // init
    public void initFields() {
        portconnectedLbl.setText((serialPort.isOpened() == false) ? "No source found" : serialPort.getPortName());
        constatusLbl.setText((serialPort.isOpened() == false) ? "Connecting To Device..." : "Device Connection Established");
        dbstatusLbl.setText((connector.isConnected() == true) ? "Database Connection OK" : "Check Database Connection");

    }

    public void initRfid() throws SerialPortException {
        serialPort = new SerialPort(rfidport);
        try {
            serialPort.openPort();//Open port
            serialPort.setParams(9600, 8, 1, 0);//Set params
            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
            serialPort.setEventsMask(mask);//Set mask
            serialPort.addEventListener(
                new SerialPortReader()
            );//Add SerialPortEventListener
//            Thread.sleep(2000);
        }
        catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }

    public void initClock(){
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat logTime = new SimpleDateFormat("hh:mm");
            currentTime = logTime.format(date);
            currentDate = logDate.format(date);
            dateLbl.setText(LocalDateTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    // end of init

    // custom methods

    // read output of rfid

    public void con(String t){
        Platform.runLater(()->{
            try{
                ResultSet ID = dao.getStudentData(t);
                if(ID.next()){
                    String stud_key = String.valueOf(ID.getInt("student_id"));
                    studidTxt.setText(String.valueOf(ID.getInt("student_id")));
                    studrfidTxt.setText(ID.getString("rfid_tag_id"));
                    studnameTxt.setText(ID.getString("student_name"));
                    studyrTxt.setText(ID.getString("student_year"));
                    studsecTxt.setText(ID.getString("student_section"));
                    int dept_key = ID.getInt("student_department");
                    studdeptTxt.setText(ID.getString("dept_name"));
                    // for image
                    InputStream is = ID.getBinaryStream("student_image");
                    OutputStream os = new FileOutputStream(new File("photo.jpg"));
                    byte[] contents = new byte[1024];
                    int size = 0;
                    while((size = is.read(contents)) !=-1){
                        os.write(contents,0,size);
                    }
                    image = new Image("file:photo.jpg",false);
                    studimageCircle.setFill(new ImagePattern(image));

                    // chk login
                    int hasData = dao.hasData(stud_key);
                    if(hasData == 0){
                        String sampleData = dao.login(stud_key,dept_key);
                    }else{
                        String timediff= "";
                        String sampleData1 = dao.logout(stud_key,dept_key,timediff);
                    }

                }else{
                    studrfidTxt.setText("No records found");
                    studnameTxt.setText("No records found");
                    studidTxt.setText("No records found");
                    studyrTxt.setText("No records found");
                    studsecTxt.setText("No records found");
                    studdeptTxt.setText("No records found");
                    image = null;
                    studimageCircle.setFill(new ImagePattern(image));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    class SerialPortReader implements SerialPortEventListener {
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue()>0){//If data is available
                if(event.getEventValue() == 10){//Check bytes count in the input buffer
                    //Read data, if 10 bytes available
                    try {
                        byte buffer[] = serialPort.readBytes(event.getEventValue());
                        String str = new String(buffer).split("\n", 2)[0].replaceAll("\\s+", "");
                        int byteSize = 0;
                        try {
                            byteSize = str.getBytes("UTF-8").length;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        if (byteSize == 8){
                            System.out.println(str);
                            con(str);
                        }
                    }
                    catch (SerialPortException ex) {
                        System.out.println(ex);
                    }
                }
            }
            else if(event.isCTS()){//If CTS line has changed state
                if(event.getEventValue() == 1){//If line is ON
                    System.out.println("CTS - ON");
                }
                else {
                    System.out.println("CTS - OFF");
                }
            }
            else if(event.isDSR()){///If DSR line has changed state
                if(event.getEventValue() == 1){//If line is ON
                    System.out.println("DSR - ON");
                }
                else {
                    System.out.println("DSR - OFF");
                }
            }
        }
    }

    // end of custom methods

    public void createPage(String title, String loc) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(loc));
        Scene sc = new Scene(root);
        sc.setFill(Color.TRANSPARENT);
        Stage secondaryStage = new Stage();
        secondaryStage.setScene(sc);
        secondaryStage.setTitle(title);
        secondaryStage.initModality(Modality.APPLICATION_MODAL);
        secondaryStage.initStyle(StageStyle.TRANSPARENT);
        secondaryStage.show();
    }



}
