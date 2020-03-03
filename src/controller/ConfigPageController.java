package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import jssc.SerialPort;
import jssc.SerialPortException;

import java.net.URL;
import java.util.ResourceBundle;

public class ConfigPageController implements Initializable {

    @FXML
    private JFXTextField rfidTxt;

    @FXML
    private JFXTextField gsmTxt;

    @FXML
    private JFXButton rfidconnectBtn;

    @FXML
    private JFXButton rfidclearBtn;

    @FXML
    private JFXButton gsmconnectBtn;

    @FXML
    private JFXButton gsmclearBtn;

    @FXML
    private Label closeBtn;

    private SerialPort serialPortRfid;
    private SerialPort serialPortGSM;
    private _pushNotification _pushNotif;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _pushNotif = new _pushNotification();
        displayInit();

        closeBtn.setOnMouseClicked(event -> {
            this.closeBtn.getScene().getWindow().hide();
            if(serialPortGSM.isOpened()){
                try {
                    serialPortGSM.closePort();
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
            if(serialPortRfid.isOpened()){
                try {
                    serialPortRfid.closePort();
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
        });
        gsmconnectBtn.setOnAction(event -> {
            if(serialPortGSM.isOpened()){
                _pushNotif.information("GSM Port Already Open", "You can now send message");
            }else{
                gsmEvent();
            }
        });
        rfidconnectBtn.setOnAction(event -> {
            if(serialPortRfid.isOpened()){
                _pushNotif.information("RFID Port Already Open", "You can now scan tag");
            }else{
                rfidEvent();
            }
        });
        gsmclearBtn.setOnAction(event -> {

            if(serialPortGSM.isOpened()){
                gsmTxt.setText("");
                try {
                    serialPortGSM.closePort();
                    _pushNotif.success("Clear Success", "Successfully Cleared Port");
                } catch (SerialPortException e) {
                    e.printStackTrace();
                    _pushNotif.failed("Clear Failed","Failed to clear ports");
                }
            }
        });
        rfidclearBtn.setOnAction(event -> {
            rfidTxt.setText("");
            if(serialPortRfid.isOpened()){
                try {
                    serialPortRfid.closePort();
                    _pushNotif.success("Clear Success", "Successfully Cleared Port");
                }catch (Exception e){
                    e.printStackTrace();
                    _pushNotif.failed("Clear Failed","Failed to clear ports");
                }
            }
        });

    }
    public void displayInit(){
        if(StudentAttendanceController.getStudentAttendanceController().rfidport != ""){
            rfidTxt.setText(StudentAttendanceController.getStudentAttendanceController().rfidport);
        }
        if(StudentAttendanceController.getStudentAttendanceController().gsmport != ""){
            gsmTxt.setText(StudentAttendanceController.getStudentAttendanceController().gsmport);
        }
    }
    public void rfidEvent(){
        String rfidPort = rfidTxt.getText();
        serialPortRfid = new SerialPort(rfidPort);
        try {
            serialPortRfid.openPort();
            _pushNotification.get_PushNotification().success("Serial Port Connect Successfully","You can use now RFID Module, Port Connected: "+rfidPort);
            StudentAttendanceController.getStudentAttendanceController().rfidport = rfidPort;
//            serialPortRfid.closePort();
        }catch (SerialPortException e) {
            e.printStackTrace();
            if(serialPortRfid.isOpened()){
                _pushNotif.information("RFID Port Already Open","You can now use rfid");
            }else{
                _pushNotification.get_PushNotification().failed("Serial Port Error","Err "+e);
            }

        }
        //Open serial port
    }
    public void gsmEvent(){
        String gsmPort = gsmTxt.getText();
        serialPortGSM = new SerialPort(gsmPort);
        try {
            serialPortGSM.openPort();
            _pushNotification.get_PushNotification().success("Serial Port Connect Successfully","You can use now GSM Module, Port Connected: "+gsmPort);
//            gsmLbl.setText("Connected To Port Number: "+gsmPort);
            StudentAttendanceController.getStudentAttendanceController().gsmport = gsmPort;
//            serialPortGSM.closePort();
        }catch (SerialPortException e) {
            e.printStackTrace();
            if(serialPortGSM.isOpened()){
                _pushNotif.information("GSM Port Already Open","You can now use gsm");
            }else{
                _pushNotification.get_PushNotification().failed("Serial Port Error","Err "+e);
            }

        }
        //Open serial port
    }
}
