package controller;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import model.ConnectionHandler;

import java.net.URL;
import java.nio.charset.StandardCharsets;
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
    private _pushNotification _pushNotif = new _pushNotification();


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

    public String getCurrentWeek() { //get current week 1-7 means monday to sunday
        LocalDate date = LocalDate.now();
        WeekFields weekFields = WeekFields.ISO;
        // check current weeks 1-7 means monday to sunday
        int currentWeeksToInt = date.get(weekFields.dayOfWeek());
        System.out.println("currentWeeksToInt"+ currentWeeksToInt);
        String currentWeeksToString = ""; // init currentWeeksToString var
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
                Date date2 = new Date() ;
                SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm") ;
                dateFormat1.format(date2);
                // end of init Date
                String valueOfWeeks = "";
                String valueOfWeeks1 = "";
                int scheduleStatus  = 0;
//                query = "select "+getCurrentWeek()+" from tardiness_endtime_tbl where dept_key = "+dept_key+" limit 1";
//                try {
//                    connection = connector.getConnection();
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//                try {
//                    prs = connection.prepareStatement(query);
//                    rs = prs.executeQuery();
//                    while(rs.next()){
//                        valueOfWeeks1 = rs.getString(1);
//                    }
//                    if(!dateFormat2.parse(dateFormat2.format(date2)).after(dateFormat2.parse(valueOfWeeks1))){
                        // mod
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
                                        //modfile
                                        query = "select " + getCurrentWeek() + " from tardiness_endtime_tbl where dept_key = " + dept_key + " limit 1";
                                        try {
                                            connection = connector.getConnection();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        try {
                                            prs = connection.prepareStatement(query);
                                            rs = prs.executeQuery();
                                            while (rs.next()) {
                                                valueOfWeeks1 = rs.getString(1);
                                            }
                                            if(!dateFormat2.parse(dateFormat2.format(date2)).after(dateFormat2.parse(valueOfWeeks1))) {
                                                policyTardiness(student_key,dept_key,dateFormat1.format(date1));
                                            }
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }

                                        // end of modfiles

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
                        // end of mod
                    }
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connector.close(connection,prs,rs);
            return "--:--:--";
        }
        //

    }
    public String logout(String student_key,int dept_key,String timediff) throws SQLException {
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
                                policyTruancy(student_key,dept_key,dateFormat1.format(date1));
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
                                policyCurfew(student_key,dept_key,dateFormat1.format(date1));
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
    public void policyTruancy(String student_key,int department_key,String timediff){
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
            truancyEvent(student_key,department_key,offense_key,timediff);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void policyCurfew(String student_key,int department_key,String timediff){
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
            curfewEvent(student_key,department_key,offense_key,timediff);

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
                        query = "insert into student_offense_tbl (`std_offense_id`,`student_key`,`offense_key`,`student_offense_count`,`offense_severity`,`offense_duration`,`offense_completedTime`,`offense_status`,`student_offense_remarks`) values (null,"+student_key+","+offense_key+","+countStudOffense+",'major','"+penaltyDuration+"','00:00','not complete','"+penaltyDescription+"')";
                    System.out.println(penaltyDuration.getClass().getName());
                        prs = connection.prepareStatement(query);
                        prs.executeUpdate();
                        description = "Student witn ID no. "+student_key+" has committed Tardiness for "+countStudOffense+" time/s, and is sanctioned with a Major Offense. An notification SMS has been sent to the students parent/guardian. you may call the parent/guardian witn the number 09051644625";
                        notifyInsert(student_key,"tardiness",departmentKey,countStudOffense,description);
                        sendSMS(student_key,"tardiness",timediff);
                }else {
                    prs.close();
                    if(departmentKey == 1){
                        query = "insert into student_offense_tbl (`std_offense_id`,`student_key`,`offense_key`,`student_offense_count`,`offense_severity`,`offense_duration`,`offense_completedTime`,`offense_status`,`student_offense_remarks`) values (null,"+student_key+","+offense_key+","+countStudOffense+",'minor','tardiness','00:00','not complete','"+timediff+"')";
                    }else {
                        query = "insert into student_offense_tbl (`std_offense_id`,`student_key`,`offense_key`,`student_offense_count`,`offense_severity`,`offense_duration`,`offense_completedTime`,`offense_status`,`student_offense_remarks`) values (null,"+student_key+","+offense_key+","+countStudOffense+",'"+severity+"','"+penaltyDuration+"','00:00','not complete','"+timediff+"')";
                    }
                    System.out.println(penaltyDuration.getClass().getName());
                    prs = connection.prepareStatement(query);
                    prs.executeUpdate();
                    System.out.println(student_key);
                    System.out.println(penaltyDescription);
                    System.out.println(departmentKey);
                    description = "Student witn ID no. "+student_key+" has committed Tardiness for "+countStudOffense+" time/s, and is sanctioned with a Minor Offense. Please Review Offense.";
                    notifyInsert(student_key,"tardiness",departmentKey,countStudOffense,description);
                    sendSMS(student_key,"tardiness",timediff);

                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                connector.close(connection,prs,rs);
            }
            // end of get offense key
    }

    public void truancyEvent(String student_key,int department,int offense_key,String timediff) throws SQLException {
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
            String description = "";
            query = "SELECT count(*) FROM student_offense_tbl as so inner join offense_tbl as o on so.offense_key = o.id where student_key = "+student_key+" and offense_key = "+offense_key+" and o.offense_severity = '"+severity+"'";
            prs = connection.prepareStatement(query);
            rs = prs.executeQuery();
            while (rs.next()){
                countStudOffense = rs.getInt(1) + 1;
            }
            if(countStudOffense >= offense_max){
                System.out.println("max");
                query = "insert into student_offense_tbl (`std_offense_id`,`student_key`,`offense_key`,`student_offense_count`,`offense_severity`,`offense_duration`,`offense_completedTime`,`offense_status`,`student_offense_remarks`) values (null,"+student_key+","+offense_key+",1,'"+severity+"','"+penaltyDuration+"','00:00','not complete','"+penaltyDescription+"')";
                prs = connection.prepareStatement(query);
                prs.executeUpdate();
                description = "Student with ID no. "+student_key+" has been detected by the system committing Truancy for "+countStudOffense+" time/s, and is sanctioned with major offense. Please review offense.";
                notifyInsert(student_key,"truancy",departmentKey,countStudOffense,description);
                sendSMS(student_key,"truancy",timediff);
            }else {
                query = "insert into student_offense_tbl (`std_offense_id`,`student_key`,`offense_key`,`student_offense_count`,`offense_severity`,`offense_duration`,`offense_completedTime`,`offense_status`,`student_offense_remarks`) values (null,"+student_key+","+offense_key+","+countStudOffense+",'"+severity+"','"+penaltyDuration+"','00:00','not complete','"+penaltyDescription+"')";
                prs = connection.prepareStatement(query);
                prs.executeUpdate();
                description = "Student with ID no. "+student_key+" has been detected by the system committing Truancy for "+countStudOffense+" time/s, and is sanctioned with major offense. Please review offense.";
                notifyInsert(student_key,"truancy",departmentKey,countStudOffense,description);
                sendSMS(student_key,"truancy",timediff);
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connector.close(connection,prs,rs);
        }
        // end of get offense key
    }

    public void curfewEvent(String student_key,int department,int offense_key,String timediff) throws SQLException {
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
            String description = "";
            prs.close();
            query = "SELECT count(*) FROM student_offense_tbl as so inner join offense_tbl as o on so.offense_key = o.id where student_key = "+student_key+" and offense_key = "+offense_key+" and o.offense_severity = '"+severity+"'";
            prs = connection.prepareStatement(query);
            rs = prs.executeQuery();
            while (rs.next()){
                countStudOffense = rs.getInt(1) + 1;
            }
            if(countStudOffense >= offense_max){
                prs.close();
                query = "insert into student_offense_tbl (`std_offense_id`,`student_key`,`offense_key`,`student_offense_count`,`offense_severity`,`offense_duration`,`offense_completedTime`,`offense_status`,`student_offense_remarks`) values (null,"+student_key+","+offense_key+",1,'"+severity+"','"+penaltyDuration+"','00:00','not complete','"+penaltyDescription+"')";
                System.out.println();
            }else {
                prs.clearParameters();
                query = "insert into student_offense_tbl (`std_offense_id`,`student_key`,`offense_key`,`student_offense_count`,`offense_severity`,`offense_duration`,`offense_completedTime`,`offense_status`,`student_offense_remarks`) values (null,"+student_key+","+offense_key+","+countStudOffense+",'"+severity+"','"+penaltyDuration+"','00:00','not complete','"+penaltyDescription+"')";
            }
            prs.close();
            prs = connection.prepareStatement(query);
            prs.executeUpdate();
            description = "Student with ID no. "+student_key+" has been detected by the system committing Curfew for "+countStudOffense+" time/s, and is sanctioned with major offense. Please review offense.";
            notifyInsert(student_key,"curfew",departmentKey,countStudOffense,description);
            sendSMS(student_key,"curfew",timediff);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connector.close(connection,prs,rs);
        }
        // end of get offense key
    }

        public void notifyInsert(String student_key,String offense_name,int department_key,int countStudOffense,String description){
//        switch (department_key){
//            case 1:
//                if(description == "tardiness"){
//                    if(countStudOffense == 2){
//                        description = "Student with ID no. "+student_key+" has been detected by the system committing Tardiness for 2 time/s, and is sanctioned with major offense. Please review offense.";
//                    }
//                }
//                break;
//            case 2:
//                break;
//            case 3:
//                break;
//            case 4:
//                break;
//        }
        query = "insert into notification_tbl (`studentNumber`,`description`,`status`,`department_key`) VALUES ("+student_key+",'"+description+"','unread',"+department_key+")";
        try {
            saveData1(query);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public  String timedifference(String time1,String time2) {
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

    public ResultSet


    getStudentInfoDetails(String query){
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

    public void sendSMS(String student_key,String offense,String timediff) throws SQLException, SerialPortException {
        serialPort = new SerialPort(StudentAttendanceController.getStudentAttendanceController().gsmport);
        serialPort.openPort();
        String student_name="",parent_fullname="",student_contact="",parent_contact="",message="";
        query = "select student_id,student_name,student_contact,parent_fullname,parent_contact from student_tbl where student_id = "+student_key+"";
        System.out.println(query);
        rs = getStudentInfoDetails(query);
        if(rs.next()){
            student_name = rs.getString("student_name");
            student_contact = rs.getString("student_contact");
            parent_fullname = rs.getString("parent_fullname");
            parent_contact = rs.getString("parent_contact");
        }
        switch(offense){
            case "tardiness":
                message = "Greetings! "+student_name+" has incurred Tardiness at ("+timediff+" on 02-05-20). Student is sanctioned with community service. Thank you!";
                break;
            case "truancy":
                message = "Greetings! We are to inform you that "+student_name+", has committed a major offense. Please expect a call or contact us at 9915668. Thank you";
                break;
            case "curfew":
                message = "Greetings! "+student_name+" has Violated Curfew of "+timediff+" on (02-05-20). Student is sanctioned with community service. Thank you!";
                break;
        }
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
            String messageString2 = "AT+CMGF=1";
//            String messageString2 = "AT+CPIN=\"7078\"";
            String messageString3 = "AT+CSCS=\"GSM\"";
//            String messageString3 = "AT+CMGF=1";
//            String messageString4 = "AT+CMGS=\"+63"+parent_contact+"\"";
            String messageString5 = message;
            String messageString4 = "";
            char enter = 13;
            char CTRLZ = 26;
            int messageNumber = 2;
            for(int i = 1; i <= messageNumber; i++){
                if(i == 1){
                    messageString4 = "AT+CMGS=\"+63"+parent_contact+"\"";
                }else{
                    messageString4 = "AT+CMGS=\"+63"+student_contact+"\"";
                }
//                if(message.length() > 150){
//
//                    int count = 0;
//                    while(_Spliter.getSplit(message).size() > count) {
//                        serialPort.writeBytes((messageString1 + enter).getBytes());
//                        Thread.sleep(1000);
//                        serialPort.writeBytes((messageString2 + enter).getBytes());
//                        Thread.sleep(1000);
//                        serialPort.writeBytes((messageString3 + enter).getBytes());
//                        Thread.sleep(1000);
//                        serialPort.writeBytes((messageString4 + enter).getBytes());
//                        Thread.sleep(1000);
//                        serialPort.writeBytes(( _Spliter.getSplit(message).get(count) + CTRLZ).getBytes());
//                        Thread.sleep(1000);
//                        System.out.println("JEROMEEEeeeee...");
//                        Thread.sleep(3000);
//                        System.out.println("JEROMEEEeeeee... complete");
//                        count++;
//                    }
//                }else{
                    serialPort.writeBytes((messageString1 + enter).getBytes());
                    Thread.sleep(1000);
                    serialPort.writeBytes((messageString2 + enter).getBytes());
                    Thread.sleep(1000);
                    serialPort.writeBytes((messageString3 + enter).getBytes());
                    Thread.sleep(1000);
                serialPort.writeBytes((messageString4 + enter).getBytes());
                    Thread.sleep(1000);
                    serialPort.writeBytes((messageString5 + CTRLZ).getBytes());
                    Thread.sleep(1000);
                    System.out.println("JEROMEEEeeeee...");
                    Thread.sleep(1000);
                    serialPort.addEventListener(new SerialPortReader());
                    Thread.sleep(3000);
                    System.out.println("JEROMEEEeeeee... complete!");
//                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            serialPort.closePort();
        }
    }
    public void response(String data){
        if(data.contains("ERROR")){
            _pushNotif.failed("Failed","Message Failed to Sent");
        }else{
            _pushNotif.success("Sent","Message Sent Success");
        }
    }

    class SerialPortReader implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0){
                try {
                    byte buffer[] = serialPort.readBytes(event.getEventValue());
                    String receivedData = new String(buffer, StandardCharsets.UTF_8);

                    Thread.sleep(4000);
                    System.out.println(receivedData.length());
                    System.out.println(receivedData);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            response(receivedData);
                        }
                    });
                }
                catch (SerialPortException | InterruptedException ex) {
                    System.out.println(ex);
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

}
