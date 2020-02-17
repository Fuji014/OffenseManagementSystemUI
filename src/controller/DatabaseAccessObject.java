package controller;
import javafx.fxml.Initializable;
import jssc.SerialPort;
import jssc.SerialPortException;
import model.ConnectionHandler;

import java.net.URL;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class DatabaseAccessObject  {
    private ConnectionHandler connector = new ConnectionHandler();
    private Connection connection;
    private PreparedStatement prs;
    private ResultSet rs;
    private String query;
    private Timestamp timestamp;
    private SerialPort serialPort;


    public void saveData(String query) throws SQLException { // save data
        try {
            connection = connector.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            prs = connection.prepareStatement(query);
            prs.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connector.close(connection, prs, null);
        }
    }
    public void saveData1(String query) throws SQLException { // save data
        try {
            connection = connector.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            prs = connection.prepareStatement(query);
            prs.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public ResultSet getStudentData(String id) throws SQLException {
        String query = "SELECT s.student_id,s.rfid_tag_id,s.student_name,s.student_year,s.student_section,s.student_course,s.student_strand,s.student_department,d.dept_name,s.student_image from student_tbl as s inner join department_tbl as d on s.student_department = d.id where s.rfid_tag_id = '"+id+"'";
        try {
            connection = connector.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            prs = connection.prepareStatement(query);
            rs = prs.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return rs;
        }
    }

    public String login(String student_key,int dept_key) throws SQLException {
        rs = null;
        Date date = new Date();
        String logDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
        String logTime = new SimpleDateFormat("hh:mm").format(date);
        // first c
        //
        // heck if student is already login in the current date
        query = "select * from record_tbl where student_key = "+student_key+" and time_in = '"+logTime+"'";
        try {
            connection = connector.getConnection();
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            prs = connection.prepareStatement(query);
            rs = prs.executeQuery();
            if(rs.next()){
//                return logTime;
            }else{
                Date date1 = new Date() ;
                SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm") ;
                dateFormat1.format(date1);
                // end of init Date
                String valueOfWeeks = "";
                int scheduleStatus  = 0;
                query = "select "+getCurrentWeek()+",status from schedule_tbl where policy = 'tardiness' and dept_key = "+dept_key+" limit 1";
                try {
                    connection = connector.getConnection();
                }catch (Exception e){
                    e.printStackTrace();
                }
                try {
                    prs = connection.prepareStatement(query);
                    rs = prs.executeQuery();
                    while(rs.next()){
                        valueOfWeeks = rs.getString(1);
                        scheduleStatus = rs.getInt(2);
                    }
                    if(scheduleStatus == 1){
                        if(dateFormat1.parse(dateFormat1.format(date1)).after(dateFormat1.parse(valueOfWeeks)))
                        {
                            String timediff = timedifference(dateFormat1.format(date1),valueOfWeeks) + " hh:mm late";

                            System.out.println("late");
                            query = "INSERT INTO `record_tbl` (`id`, `student_key`, `date`, `time_in`, `time_out`, `login_remarks`) VALUES (NULL, "+student_key+", '"+logDate+"', '"+logTime+"', '', '"+timediff+"')";
                            connection = connector.getConnection();
                            prs = connection.prepareStatement(query);
                            prs.executeUpdate();
                            if(scheduleStatus == 1){
                                policyTardiness(student_key,dept_key,timediff);
                            }

                        }else{
                            System.out.println("on time");
                            query = "INSERT INTO `record_tbl` (`id`, `student_key`, `date`, `time_in`, `time_out`, `login_remarks`) VALUES (NULL, "+student_key+", '"+logDate+"', '"+logTime+"', '', 'on time')";
                            connection = connector.getConnection();
                            prs = connection.prepareStatement(query);
                            prs.executeUpdate();
                        }
                    }

                    // end of execution
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connector.close(connection,prs,rs);
            return "--:--:--";
        }
        //

    }
    public String logout(String student_key,int dept_key) throws SQLException {
        rs = null;
        Date date = new Date();
        String logDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
        String logTime = new SimpleDateFormat("hh:mm").format(date);
        query = "select * from `record_tbl` where student_key = "+student_key+" and date = '"+logDate+"' and time_out = '' order by id desc limit 1";
        try {
            connection = connector.getConnection();
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            prs = connection.prepareStatement(query);
            rs = prs.executeQuery();
            if(rs.next()){
                query = "select * from `record_tbl` where student_key = "+student_key+" and date = '"+logDate+"' and time_out != '' order by id desc limit 1";
                connection = connector.getConnection();
                prs = connection.prepareStatement(query);
                rs = prs.executeQuery();
                if(rs.next()){
                    query = "update record_tbl set time_out = '"+logTime+"', logout_remarks = '' where student_key = "+student_key+" order by id desc limit 1";
                    prs = connection.prepareStatement(query);
                    prs.executeUpdate();
                }else{
                    query = "update record_tbl set time_out = '"+logTime+"', logout_remarks = 'logout' where student_key = "+student_key+" order by id desc limit 1";
                    prs = connection.prepareStatement(query);
                    prs.executeUpdate();
                    Date date1 = new Date() ;
                    SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm") ;
                    dateFormat1.format(date1);
                    // end of init Date
                    String valueOfWeeks = "";
                    int scheduleStatus  = 0;
                    // truancy
                    query = "select "+getCurrentWeek()+",status from schedule_tbl where policy = 'truancy' and dept_key = "+dept_key+" limit 1";
                    try {
                        connection = connector.getConnection();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    try {
                        prs = connection.prepareStatement(query);
                        rs = prs.executeQuery();
                        while (rs.next()) {
                            valueOfWeeks = rs.getString(1);
                            scheduleStatus = rs.getInt(2);
                        }
                        System.out.println(dateFormat1.format(date1));
                        System.out.println(dateFormat1.parse(valueOfWeeks));
                        if(scheduleStatus == 1){
                            if(!dateFormat1.parse(dateFormat1.format(date1)).after(dateFormat1.parse(valueOfWeeks)))
                            {
                                System.out.println("truancy");
                                policyTruancy(student_key,dept_key);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    // end of truancy
                    // curfew
                    query = "select "+getCurrentWeek()+",status from schedule_tbl where policy = 'curfew' and dept_key = "+dept_key+" limit 1";
                    try {
                        connection = connector.getConnection();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    try {
                        prs = connection.prepareStatement(query);
                        rs = prs.executeQuery();
                        while (rs.next()) {
                            valueOfWeeks = rs.getString(1);
                            scheduleStatus = rs.getInt(2);
                        }
                        System.out.println(dateFormat1.format(date1));
                        System.out.println(dateFormat1.parse(valueOfWeeks));
                        if(scheduleStatus == 1){
                            if(dateFormat1.parse(dateFormat1.format(date1)).after(dateFormat1.parse(valueOfWeeks)))
                            {
                                System.out.println("curfew!");
                                policyCurfew(student_key,dept_key);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    // end of curfew
                }
            }else{
                query = "INSERT INTO `record_tbl` (`id`, `student_key`, `date`, `time_in`, `time_out`, `login_remarks`) VALUES (NULL, "+student_key+", '"+logDate+"', '"+logTime+"', '', '')";
                connection = connector.getConnection();
                prs = connection.prepareStatement(query);
                prs.executeUpdate();
            }


        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connector.close(connection,prs,null);
            return "jerome";
        }

    }

    public int hasData(String student_key) throws SQLException {
        rs = null;
        Date date = new Date();
        String logDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
        String logTime = new SimpleDateFormat("hh:mm").format(date);
        int count = 0;
        query = "select count(*) from record_tbl where student_key = "+student_key+" and date = '"+logDate+"'";
        try {
            connection = connector.getConnection();
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            prs = connection.prepareStatement(query);
            rs = prs.executeQuery();
            while (rs.next()){
                count = rs.getInt(1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connector.close(connection,prs,rs);
            return count;
        }
    }

    public static String getCurrentWeek() { //get current week 1-7 means monday to sunday
        LocalDate date = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        // check current weeks 1-7 means monday to sunday
        int currentWeeksToInt = date.get(weekFields.weekOfWeekBasedYear());
        String currentWeeksToString = ""; // init currentWeeksToInt var
        switch(currentWeeksToInt){
            case 1:
                currentWeeksToString = "monday";
                break;
            case 2:
                currentWeeksToString = "tuesday";
                break;
            case 3:
                currentWeeksToString = "wednesday";
                break;
            case 4:
                currentWeeksToString = "thursday";
                break;
            case 5:
                currentWeeksToString = "friday";
                break;
            case 6:
                currentWeeksToString = "saturday";
                break;
            case 7:
                currentWeeksToString = "sunday";
                break;
        }
        // end of check current weeks
        return currentWeeksToString;
    }

    public void policyTardiness(String student_key,int department_key,String timediff){
        int offense_key=0;
        switch (department_key){
            case 1:
                offense_key = 225;
                break;
            case 2:
                offense_key = 151;
                break;
            case 3:
                offense_key = 73;
                break;
            case 4:
                offense_key = 0;
                break;
        }
        try{
            System.out.println(offense_key);
            tardinessEvent(student_key,department_key,offense_key,timediff);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void policyTruancy(String student_key,int department_key){
        int offense_key=0;
        switch (department_key){
            case 1:
                offense_key = 226;
                break;
            case 2:
                offense_key = 179;
                break;
            case 3:
                offense_key = 109;
                break;
            case 4:
                offense_key = 0;
                break;
        }
        try{
            truancyEvent(student_key,department_key,offense_key);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void policyCurfew(String student_key,int department_key){
        int offense_key=0;
        switch (department_key){
            case 1:
                offense_key = 233;
                break;
            case 2:
                offense_key = 154;
                break;
            case 3:
                offense_key = 79;
                break;
            case 4:
                offense_key = 0;
                break;
        }
        try{
            curfewEvent(student_key,department_key,offense_key);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void tardinessEvent(String student_key,int department,int offense_key,String timediff) throws SQLException {
        // get offense key
            try {
                // select severity and dept key of offense_tbl
                String severity = "";
                String description = "";
                String joinNotifDescription="";
                int departmentKey = 0;
                query = "select offense_description,offense_severity,dept_key from offense_tbl where id = "+offense_key+" limit 1";
                prs = connection.prepareStatement(query);
                rs = prs.executeQuery();
                while (rs.next()){
                    description = rs.getString(1);
                    severity = rs.getString(2);
                    departmentKey = rs.getInt(3);
                }
                joinNotifDescription = "You Have committed ";
                // select max and duration from policy
                String penaltyDescription="",penaltyDuration = "";
                int offense_max = 0;
                query = "select offense_max,penalty_duration,penalty_description  from policy_tbl WHERE department_key = "+departmentKey+" and offense_severity = '"+severity+"'";
                prs = connection.prepareStatement(query);
                rs = prs.executeQuery();
                while (rs.next()){
                    offense_max = rs.getInt(1);
                    penaltyDuration = rs.getString(2);
                    penaltyDescription = rs.getString(3);
                }
                // count in current table
                int countStudOffense = 0;
                query = "SELECT count(*) FROM student_offense_tbl as so inner join offense_tbl as o on so.offense_key = o.id where student_key = "+student_key+" and offense_key = "+offense_key+" and o.offense_severity = '"+severity+"'";
                prs = connection.prepareStatement(query);
                rs = prs.executeQuery();
                while (rs.next()){
                    countStudOffense = rs.getInt(1) + 1;
                }

                if(countStudOffense >= offense_max){
                    prs.close();
                        query = "insert into student_offense_tbl (`std_offense_id`,`student_key`,`offense_key`,`student_offense_count`,`offense_severity`,`offense_duration`,`offense_completedTime`,`offense_status`,`student_offense_remarks`) values (null,"+student_key+","+offense_key+","+countStudOffense+",'major','"+penaltyDuration+"','00:00',0,'"+penaltyDescription+"')";
                        prs = connection.prepareStatement(query);
                        prs.executeUpdate();
                        notifyInsert(student_key,penaltyDescription,departmentKey);
                        sendSMS(student_key,severity,"test",description,description);
                }else {
                    prs.close();
                    if(departmentKey == 1){
                        query = "insert into student_offense_tbl (`std_offense_id`,`student_key`,`offense_key`,`student_offense_count`,`offense_severity`,`offense_duration`,`offense_completedTime`,`offense_status`,`student_offense_remarks`) values (null,"+student_key+","+offense_key+","+countStudOffense+",'minor','tardiness','00:00',0,'"+timediff+"')";
                    }else {
                        query = "insert into student_offense_tbl (`std_offense_id`,`student_key`,`offense_key`,`student_offense_count`,`offense_severity`,`offense_duration`,`offense_completedTime`,`offense_status`,`student_offense_remarks`) values (null,"+student_key+","+offense_key+","+countStudOffense+",'"+severity+"','"+penaltyDuration+"','00:00',0,'"+timediff+"')";
                    }
                    prs = connection.prepareStatement(query);
                    prs.executeUpdate();
                    System.out.println(student_key);
                    System.out.println(penaltyDescription);
                    System.out.println(departmentKey);

                    notifyInsert(student_key,penaltyDescription,departmentKey);
                    sendSMS(student_key,severity,"test",description,description);

                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                connector.close(connection,prs,rs);
            }
            // end of get offense key
    }

    public void truancyEvent(String student_key,int department,int offense_key) throws SQLException {
        // get offense key
        try {
            // select severity and dept key of offense_tbl
            String severity = "";
            int departmentKey = 0;
            query = "select offense_severity,dept_key from offense_tbl where id = "+offense_key+" limit 1";
            prs = connection.prepareStatement(query);
            rs = prs.executeQuery();
            while (rs.next()){
                severity = rs.getString(1);
                departmentKey = rs.getInt(2);
            }

            // select max and duration from policy
            String penaltyDescription="",penaltyDuration = "";
            int offense_max = 0;
            query = "select offense_max,penalty_duration,penalty_description  from policy_tbl WHERE department_key = "+departmentKey+" and offense_severity = '"+severity+"'";
            prs = connection.prepareStatement(query);
            rs = prs.executeQuery();
            while (rs.next()){
                offense_max = rs.getInt(1);
                penaltyDuration = rs.getString(2);
                penaltyDescription = rs.getString(3);
            }
            // count in current table
            int countStudOffense = 0;
            query = "SELECT count(*) FROM student_offense_tbl as so inner join offense_tbl as o on so.offense_key = o.id where student_key = "+student_key+" and offense_key = "+offense_key+" and o.offense_severity = '"+severity+"'";
            prs = connection.prepareStatement(query);
            rs = prs.executeQuery();
            while (rs.next()){
                countStudOffense = rs.getInt(1) + 1;
            }
            if(countStudOffense >= offense_max){
                System.out.println("max");
                query = "insert into student_offense_tbl (`std_offense_id`,`student_key`,`offense_key`,`student_offense_count`,`offense_severity`,`offense_duration`,`offense_completedTime`,`offense_status`,`student_offense_remarks`) values (null,"+student_key+","+offense_key+",1,'"+severity+"','"+penaltyDuration+"','00:00',0,'"+penaltyDescription+"')";
                prs = connection.prepareStatement(query);
                prs.executeUpdate();
                notifyInsert(student_key,penaltyDescription,departmentKey);
            }else {
                System.out.println("wow");
                query = "insert into student_offense_tbl (`std_offense_id`,`student_key`,`offense_key`,`student_offense_count`,`offense_severity`,`offense_duration`,`offense_completedTime`,`offense_status`,`student_offense_remarks`) values (null,"+student_key+","+offense_key+","+countStudOffense+",'"+severity+"','"+penaltyDuration+"','00:00',0,'"+penaltyDescription+"')";
                prs = connection.prepareStatement(query);
                prs.executeUpdate();
                notifyInsert(student_key,penaltyDescription,departmentKey);
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connector.close(connection,prs,rs);
        }
        // end of get offense key
    }
    public void notifyInsert(String student_key,String description,int department_key){
        query = "insert into notification_tbl (`studentNumber`,`description`,`status`,`department_key`) VALUES ("+student_key+",'"+description+"','unread',"+department_key+")";
        try {
            saveData1(query);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void curfewEvent(String student_key,int department,int offense_key) throws SQLException {
        // get offense key
        try {
            // select severity and dept key of offense_tbl
            String severity = "";
            int departmentKey = 0;
            prs.close();
            query = "select offense_severity,dept_key from offense_tbl where id = "+offense_key+" limit 1";
            prs = connection.prepareStatement(query);
            rs = prs.executeQuery();
            while (rs.next()){
                severity = rs.getString(1);
                departmentKey = rs.getInt(2);
            }

            // select max and duration from policy
            String penaltyDescription="",penaltyDuration = "";
            int offense_max = 0;
            query = "select offense_max,penalty_duration,penalty_description  from policy_tbl WHERE department_key = "+departmentKey+" and offense_severity = '"+severity+"'";
            prs.close();
            prs = connection.prepareStatement(query);
            rs = prs.executeQuery();
            while (rs.next()){
                offense_max = rs.getInt(1);
                penaltyDuration = rs.getString(2);
                penaltyDescription = rs.getString(3);
            }
            // count in current table
            int countStudOffense = 0;
            prs.close();
            query = "SELECT count(*) FROM student_offense_tbl as so inner join offense_tbl as o on so.offense_key = o.id where student_key = "+student_key+" and offense_key = "+offense_key+" and o.offense_severity = '"+severity+"'";
            prs = connection.prepareStatement(query);
            rs = prs.executeQuery();
            while (rs.next()){
                countStudOffense = rs.getInt(1) + 1;
            }
            if(countStudOffense >= offense_max){
                prs.close();
                query = "insert into student_offense_tbl (`std_offense_id`,`student_key`,`offense_key`,`student_offense_count`,`offense_severity`,`offense_duration`,`offense_completedTime`,`offense_status`,`student_offense_remarks`) values (null,"+student_key+","+offense_key+",1,'"+severity+"','"+penaltyDuration+"','00:00',0,'"+penaltyDescription+"')";
            }else {
                prs.clearParameters();
                query = "insert into student_offense_tbl (`std_offense_id`,`student_key`,`offense_key`,`student_offense_count`,`offense_severity`,`offense_duration`,`offense_completedTime`,`offense_status`,`student_offense_remarks`) values (null,"+student_key+","+offense_key+","+countStudOffense+",'"+severity+"','"+penaltyDuration+"','00:00',0,'"+penaltyDescription+"')";
            }
            notifyInsert(student_key,penaltyDescription,departmentKey);
            prs.close();
            prs = connection.prepareStatement(query);
            prs.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connector.close(connection,prs,rs);
        }
        // end of get offense key
    }

    public  String timedifference(String time1,String time2) {
        //String time1 = "20:30";
        //String time2 = "19:00";
        String timediff = "";
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        try {
            Date date1 = format.parse(time1);
            Date date2 = format.parse(time2);
            long diff = date1.getTime() - date2.getTime();
            long diffMinutes = diff / (60 * 1000);
            long hours = diffMinutes / 60; //since both are ints, you get an int
            long minutes = diffMinutes % 60;
            System.out.printf("%d:%02d", hours, minutes);
            timediff = String.format("%d:%02d", hours, minutes);
        }
        catch (Exception e) {
            e.printStackTrace();
        }finally {
            return timediff;
        }
    }

    public ResultSet getStudentInfoDetails(String query){
        try {
            connection = connector.getConnection();
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            prs = connection.prepareStatement(query);
            rs = prs.executeQuery();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            return rs;
        }
    }

    public void sendSMS(String student_key,String severity,String offense, String punishment,String remarks) throws SQLException, SerialPortException {
        serialPort = new SerialPort("COM5");
        serialPort.openPort();
        String student_name="",parent_fullname="",parent_contact="",message="";
        query = "select student_id,student_name,parent_fullname,parent_contact from student_tbl where student_id = "+student_key+"";
        System.out.println(query);
        rs = getStudentInfoDetails(query);
        if(rs.next()){
            student_name = rs.getString("student_name");
            parent_fullname = rs.getString("parent_fullname");
            parent_contact = rs.getString("parent_contact");
        }
        String message1 = "Greetings Mr/Ms. "+parent_fullname+", This message is to inform you that "+student_name+", with ID number "+student_key+" has committed a violation against the school's policy. He/She violated a "+severity+" offense under subjection "+offense+", and is subjected to "+punishment+". Remarks: "+remarks+". For more details and concerns, Please contact us at 09087118184. Thankyou";
        message = "jerome gabat apple jean garcia yan e ";
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
