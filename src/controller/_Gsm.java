package controller;

import jssc.SerialPort;
import jssc.SerialPortException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class _Gsm {
    private SerialPort serialPort;
    private String query;
    private DatabaseAccessObject dao = new DatabaseAccessObject();
    private ResultSet rs;
    private static _Gsm instance;

    public void sendSMS(String student_key,String severity,String offense, String punishment,String remarks) throws SQLException, SerialPortException {
        serialPort = new SerialPort("COM5");
        serialPort.openPort();
        String student_name="",parent_fullname="",parent_contact="",message="";
        query = "select student_id,student_name,parent_fullname,parent_contact from student_tbl where student_id = "+student_key+"";
        System.out.println(query);
        rs = dao.getStudentInfoDetails(query);
        if(rs.next()){
            student_name = rs.getString("student_name");
            parent_fullname = rs.getString("parent_fullname");
            parent_contact = rs.getString("parent_contact");
        }
        message = "Greetings Mr/Ms. "+parent_fullname+", This message is to inform you that "+student_name+", with ID number "+student_key+" has committed a violation against the school's policy. He/She violated a "+severity+" offense under subjection "+offense+", and is subjected to "+punishment+". Remarks: "+remarks+". For more details and concerns, Please contact us at 09087118184. Thankyou";
        if (serialPort.isOpened()) { // check if open
            System.out.println("Port is open :)");
        } else {
            System.out.println("Failed to open port :(");
            return;
        }
        try {
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);
            String messageString1 = "AT";
//            String messageString2 = "AT+CPIN=\"7078\"";
            String messageString3 = "AT+CSCS=\"GSM\"";
//            String messageString3 = "AT+CMGF=1";
            String messageString4 = "AT+CMGS=\"+63"+parent_contact+"\"";
            String messageString5 = message;
            char enter = 13;
            char CTRLZ = 26;

            if(message.length() > 150){
                int count = 0;

                while(_Spliter.getSplit(message).size() > count) {
                    serialPort.writeBytes((messageString1 + enter).getBytes());
                    Thread.sleep(1000);
                    serialPort.writeBytes((messageString3 + enter).getBytes());
                    Thread.sleep(1000);
                    serialPort.writeBytes((messageString4 + enter).getBytes());
                    Thread.sleep(1000);
                    serialPort.writeBytes(( _Spliter.getSplit(message).get(count) + CTRLZ).getBytes());
                    Thread.sleep(1000);
                    System.out.println("JEROMEEEeeeee...");
                    Thread.sleep(3000);
                    System.out.println("JEROMEEEeeeee... complete");
                    count++;
                }
            }else{
                serialPort.writeBytes((messageString1 + enter).getBytes());
                Thread.sleep(1000);
                serialPort.writeBytes((messageString3 + enter).getBytes());
                Thread.sleep(1000);
                serialPort.writeBytes((messageString4 + enter).getBytes());
                Thread.sleep(1000);
                serialPort.writeBytes((messageString5 + CTRLZ).getBytes());
                Thread.sleep(1000);
                System.out.println("JEROMEEEeeeee...");
                Thread.sleep(3000);
                System.out.println("JEROMEEEeeeee... complete!");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            serialPort.closePort();
        }
    }
}