package bgu.spl.net.impl.BGRSServer;



import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Passive object representing the Database where all courses and users are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add private fields and methods to this class as you see fit.
 */
public class Database {

    private static Database database = null;
    //courses database private ConcurrentHashMap<>
    private static ConcurrentHashMap<Integer, Course> courses = new ConcurrentHashMap<Integer, Course>();
    private static ConcurrentHashMap<String, Student> users = new ConcurrentHashMap<String, Student>();
    private static ConcurrentHashMap<Integer, LinkedList<String>> studentsOfCourse = new ConcurrentHashMap<Integer, LinkedList<String>>();

    //private static ConcurrentHashMap<String, Admin> admins = new ConcurrentHashMap<String, Admin>();
    //to prevent user from creating new Database
    private Database() {

    }

    /**
     * Retrieves the single instance of this class.
     */
    public static Database getInstance() {
        if(database==null){
           database = new Database();
        }
        return database;
    }

    /**
     * loades the courses from the file path specified
     * into the Database, returns true if successful.
     */
    boolean initialize(String coursesFilePath) {
        try {

            File file = new File(coursesFilePath);
            BufferedReader br = new BufferedReader(new FileReader(file));

            LinkedList<Course> allCourses = new LinkedList<Course>();
            String st;
            int order = 0;
            while ((st = br.readLine()) != null) {
                order++;

                //Separating the 4 parts of the line.
                String courseNum = st.substring(0, st.indexOf('|'));
                int courseNumInt = Integer.parseInt(courseNum);
                st = st.substring(st.indexOf('|')+1);
                String courseName = st.substring(0, st.indexOf('|'));
                st = st.substring(st.indexOf('|')+1);
                String courseList = st.substring(1, st.indexOf('|')-1);
                String courseListCheck = st.substring(0, st.indexOf('|'));
                st = st.substring(st.indexOf('|')+1);
                String courseLimit = st;
                int courseLimitInt = Integer.parseInt(courseLimit);




                //Going over the kdam list, and turning it into a real list
                LinkedList<Integer> kdamList = new LinkedList<Integer>();
                String tempList = courseList;
                boolean enterWhile = true;
                if(courseListCheck.equals("[]")){
                    enterWhile = false;
                }
                while(enterWhile){
                    if(tempList.indexOf(',')>0) {
                        String oneNum = tempList.substring(0, tempList.indexOf(','));
                        tempList = tempList.substring(tempList.indexOf(',')+1);
                        kdamList.add(Integer.parseInt(oneNum));
                    }
                    else {
                        String lastNum = tempList;
                        kdamList.add(Integer.parseInt(lastNum));
                        break;
                    }
                }

                //Creating the Course. First, we add an empty LinkedList<Course> for the Kdam, we add it later.
                Course course = new Course(courseNumInt, order, courseName, new LinkedList<Course>(), kdamList, courseLimitInt);
                courses.put(courseNumInt, course);
                allCourses.add(course);
            }

            //When every course is made, every Course gets its kdamList initialized, with this double loop.
            for(Course course: allCourses){
                for(Integer kdam: course.getKdamNum()){
                    course.addKdam(courses.get(kdam));
                }
            }
            return true;
        }
        catch(FileNotFoundException e){e.printStackTrace();}
        catch(IOException e){e.printStackTrace();}


        return false;
    }


    //CASE 1: ADMINREG
    public static boolean ADMINREG(String user, String pass){
        return (users.putIfAbsent(user, new Student(user, pass, true)) == null);
    }
    //CASE 2: STUDENTREG
    public static boolean STUDENTREG(String user, String pass){
        return (users.putIfAbsent(user, new Student(user, pass, false)) == null);
    }
    //CASE 3: LOGIN
    public static boolean LOGIN(String user, String pass){

        Student s = users.get(user);
            if(s!=null) {
                s.getLock().lock();
                return s.login(pass);
            }
            return false;
    }
    //CASE 4: LOGOUT
    //This function checks if our user is already logged in. If so, log him out and return true. If not, return false.
    public static boolean LOGOUT(String username){
        Student user = users.get(username);
        if(user!=null && user.isLogin())
            return user.logout();

        return false;
        }

        //CASE 5: COURSEREG
    public static  boolean COURSEREG(String username, int courseNum) {
        Course course = courses.get(courseNum);
        if (course != null) {
            course.getLock().lock();
            Student user = users.get(username);
            if (user != null && user.isLogin() && !user.isAdmin()) {
                if (user.register(course) && course.register(username)) {
                    course.getLock().unlock();
                    return true;
                }
            }
            course.getLock().unlock();
        }
        return false;
    }
    //CASE 6: KDAMCHECK
    public static String KDAMCHECK(String username, int courseNum){
        Student user = users.get(username);
        Course course = courses.get(courseNum);
        if (user!=null && course!=null  && user.isLogin()) {
            String s = course.getKdamString();
            return s;
        }
        return "error";
    }
    //CASE 7: COURSESTAT
    public static String COURSESTAT(String username, int courseNum){
        Course course = courses.get(courseNum);
        if(course!=null) {
            course.getLock().lock();
            Student user = users.get(username);
            if (user != null && user.isLogin() && user.isAdmin()) {
                String ans = "Course: (" + courseNum + ") " + course.getName() + "|" + "Seats Available: " +
                        course.seatStat() + "|" + "Students Registered: " + course.getStudents();
                course.getLock().unlock();
                return ans;
            }
            course.getLock().unlock();
        }

        return "error";
    }
    //CASE 8: STUDENTSTAT

    public static String STUDENTSTAT(String username, String studentName){
        Student admin = users.get(username);
        Student student = users.get(studentName);
        if(admin!=null && student!=null && admin.isAdmin() && !student.isAdmin() && admin.isLogin()) {
            synchronized (student) {
                String ans = "Student: " + studentName + "|" + "Courses: " + student.getStringCourses();
                return ans;
            }
        }
        return "error";
    }
    //CASE 9: ISREGISTERED
    public static String ISREGISTERED(String username, int courseNum){
        Student user = users.get(username);
        Course course = courses.get(courseNum);
        if (user!=null && !user.isAdmin() && user.isLogin() && course!=null) {
            if(course.isRegistered(username))
                return "REGISTERED";
            else
                return "NOT REGISTERED";
            }
        return "error";
    }
    //CASE 10: UNREGISTER
    public static  boolean UNREGISTER(String username, int courseNum) {
        Course course = courses.get(courseNum);
        if (course != null) {
            course.getLock().lock();
            Student user = users.get(username);
            if (user != null && user.isLogin() && !user.isAdmin()) {
                if (course.isRegistered(username)) {
                    user.unregister(course);
                    course.unregister(username);
                    course.getLock().unlock();
                    return true;
                }

            }
            course.getLock().unlock();
        }

        return false;
    }
    //CASE 11: MYCOURSES
    public static String MYCOURSES(String username) {
        Student user = users.get(username);
        if (user!=null && !user.isAdmin() &&  user.isLogin()) {
            //We choose to sort list only when calling for the function, to save runtime
            return user.getStringCourses();
        }
        return "error";
    }


}