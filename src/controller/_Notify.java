package controller;

public class _Notify {
    private DatabaseAccessObject dao;
    private String query;
    private static _Notify instance;
    public _Notify(){ this.instance=this;}
    public static _Notify get_Notify(){return instance;}
    public void notifyInsert(String student_key,String description,int department_key){
        System.out.println(student_key);
        System.out.println(description);
        System.out.println(department_key);
        query = "insert into notification_tbl (`studentNumber`,`description`,`status`,`department_key`) VALUES ("+student_key+",'"+description+"','unread',"+department_key+")";
        try {
            dao.saveData(query);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
