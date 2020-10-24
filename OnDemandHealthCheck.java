//OnDemandHealthCheck.java
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;
import java.util.List;
import java.io.*;
import java.net.*; //HttpURLConnection
import java.io.File;
import java.lang.*;  //THREAD_SLEEP
import java.net.InetAddress; //IP & HOSTNAME
import java.net.UnknownHostException; //HOSTNAME
import java.text.SimpleDateFormat; //TIME_STAMP
import java.util.Date;   //TIME_STAMP
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.*;
//import java.util.LinkedList;
//import java.util.Queue;
//
//import org.apache.commons.io.FileUtils;
//
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait; //WebDriverWait
//import org.openqa.selenium.support.ui.FluentWait; //Fluent Wait
import org.openqa.selenium.support.ui.ExpectedConditions; //ExpectedConditions
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import com.thoughtworks.selenium.SeleniumException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
//
import org.openqa.selenium.JavascriptExecutor;		
//
import org.riversun.slacklet.Slacklet;
import org.riversun.slacklet.SlackletRequest;
import org.riversun.slacklet.SlackletResponse;
import org.riversun.slacklet.SlackletService;
import org.riversun.slacklet.SlackletSession;
import org.riversun.slacklet.SletDefaultPersistManager;
import org.riversun.slacklet.SletPersistManager;
import org.riversun.xternal.simpleslackapi.SlackAttachment;
//

public class OnDemandHealthCheck {
    //GLOBAL_VARS
    public static String PROC = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
    public static String PID = PROC.split("@")[0];
    //
    public static boolean FLAMEON = false;
    public static boolean LOOP = false;
    //QUEUES
    public static Queue<String> testQueue = new LinkedList<>(); 
    //SERVICE_VARS
    public static SlackletService slackService;
    //BOT_VARS
    public static String botToken = "xoxb-236382243602-691794813056-0mX1bLIZiglqS0qZ8lOj0Lvp";
    public static String channelName = "heavens_to_murgatroyd";
    public static long BORED; //KEEP_ALIVE_GLOBAL
    public static long ALIVE; //CONNECTION_TIMEOUT
    public static boolean SLEEP = false; //SLEEP_STATE

    /***** MAIN *****/
    public static void main(String[] args) throws IOException {
        //PRINT_PID
        System.out.println("MY_NAME_IS: "+ PROC);
        System.out.println("MY_PID_IS: "+ PID);
        // changing the name of Main thread
        Thread t = Thread.currentThread();
        t.setName("OnDemanHC");
        System.out.println("Main Thread NAME: " + t.getName());
        //PUT_ARRAY_HERE_OR_MAKE_PROPERTIES_FILE
        configFiles();
        //REALTIME_CMD_VIA_CHAT
        //START_UP_IMAGE
        startUpImg();
        //START_RTM_API_CONNECTION
        startSlackBot();
        //TEST_NETWORK
        testNet();
        //START_THREAD
        threadKeepAlive KAT1 = new threadKeepAlive("Thread-1", slackService, channelName);
        KAT1.start(); //START_KEEP_ALIVE_THREAD
    }//END_MAIN


    /***** CLASS FUNCTIONS *****/

    //LOOK_FOR_TEST_FILES
    public static void configFiles() {
        File folder = new File("webtest");
        String[] files = folder.list();
        for (String file : files) {
            if (file.matches(("(?:.*.properties)"))) {
                String[] testname = file.split("\\.properties");
                System.out.println("FILE FOUND: "+file);
                System.out.println("WebTest: "+testname[0]);
            }
        }
    }

    //AUTO_WEBTEST_EVERY_15MIN_SKIP_IF_ONDEMAND
    public static void autoTest() {
        if (!LOOP){
            WebTest.slack("Starting AutoTesting... ");
        }
        LOOP = false;
        //BUILD_TEST_QUEUE
        String nextTest = testQueue.poll(); 
        if ( nextTest != null ) {
            //RUN_TEST        
            System.out.println("The Next Test: " + nextTest); 
            threadWait(1001);
            String[] TEST = new String [] {"webtest/"+nextTest+".properties"};
            WebTest.main(TEST);
            threadWait(1001);
            WebTest.slack("Done with "+"@"+nextTest+"@"+" test");
            threadWait(1001);
        } else {
            //FILL_QUEUE
            System.out.println("[[ ADDING TESTS TO QUEUE ]]");
            File folder = new File("webtest");
            String[] files = folder.list();
            for (String file : files) {
                if (file.matches(("(?:.*.properties)"))) {
                    String[] testname = file.split("\\.properties");
                    //System.out.println("FILE FOUND: "+file);
                    System.out.println("ADD WebTest: "+testname[0]);
                    testQueue.add(testname[0]); 
                }
            }
            System.out.println("Queued Tests" + testQueue);
            LOOP = true;
            autoTest();
        }
    }


    //START_UP_SLACK_CONNCTION
    public static void startSlackBot() {
        try {
            if (FLAMEON) {
                slackService.stop(); //STOP
            }
            slackService = new SlackletService(botToken);
            slackService.addSlacklet(slackListener);//END_RTM_API_CONNECTION
            slackService.start(); //START_LOOP
            FLAMEON = true;
            //
            if (ALIVE > 0) {
                long NOW = System.currentTimeMillis();
                long connUptime = NOW - ALIVE;
                System.out.printf("Reconnection Interval: %d:ms %d:s %d:m %d:h %d:d %d:y %n", connUptime, connUptime / 1000, connUptime / 60000, connUptime / 60000 / 60, connUptime / 60000 / 60 / 24, connUptime / 60000 / 60 / 24 / 365 );
                String RECONNECT = String.format("Reconnection Interval: %d:ms %d:s %d:m %d:h %d:d %d:y %n", connUptime, connUptime / 1000, connUptime / 60000, connUptime / 60000 / 60, connUptime / 60000 / 60 / 24, connUptime / 60000 / 60 / 24 / 365 );
                slackService.sendMessageTo(channelName, RECONNECT);
            }
            ALIVE = System.currentTimeMillis(); //CONNECTION_ALIVE
            //
        } catch(IOException e) {
            System.out.println("ERROR Starting (startSlackBot): " + e);
            threadWait(2000); //wait_2sec
            System.exit(-1); //DIE
        }
    }

    //START_UP_IMAGE
    public static void startUpImg() {
        try {
            threadWait(1001);
            //SEND_STARTUP_IMG
            SlackletService slackService_img = new SlackletService(botToken);
            slackService_img.start();
            final String imageUrl = "http://edumuch.com/wp-content/uploads/2016/03/Standardized-Testing.gif";
            final String imageUrlFallBack = "https://www.promoter.io/blog/wp-content/uploads/2017/06/636078412599573926-2019265753_Help-1446575640.gif";
            final SlackAttachment attchImage = new SlackAttachment();
            attchImage.setTitle("Standard Automation Testing (SAT)");
            attchImage.setText("Version HAL.9001.0.0000");
            attchImage.setFallback(imageUrlFallBack);
            attchImage.setColor("#ffffff");
            attchImage.setImageUrl(imageUrl);
            slackService_img.sendMessageTo(channelName, "Starting the OnDeman healthcheck....\n(Have Patience) Standard Slack rate limits apply:\n https://api.slack.com/docs/rate-limits", attchImage);
            slackService_img.stop();
            System.out.println("[[ startUpImg ]]: Done");
        } catch (ConnectException e) {
            System.out.println("[[ ERROR_FIXUP@startUpImg ]] Caught ConnectException: " +  e);
            threadWait(3000); //wait_3sec
            startUpImg();
        } catch (IOException e) {
            System.out.println("ERROR startUpImg (slackService_img): " + e);
            threadWait(3000); //wait_3sec
            System.exit(-1); //DIE
        }
    }

    //CLEAN_UP_REMOVE_MARKDOWN_DATA
    public static String markdown(String mdata) {
        //REMOVE_MARKDOWN_TAGS_FROM_SLACK
        System.out.println("Scrubbing data....");
        String MARK = mdata.replaceAll("<(.*)>","@$1@"); //MARK
        System.out.println("MARK: "+MARK);
        String HASH = MARK.replaceAll("@(.*)\\|(.*)@","@$1#$2@"); //HASH
        System.out.println("HASH: "+HASH);
        String FRONT = HASH.replaceAll("@.*\\#",""); //FRONT
        System.out.println("FRONT: "+FRONT);
        String BACK = FRONT.replaceAll("@",""); //BACK
        System.out.println(BACK);
        System.out.println("CLEAN DATA: "+BACK);
        return BACK;
    }

    //THREADWAIT
    public static void threadWait(long timeout) {
        try {
            // thread to sleep for 1 sec = 1000 milliseconds
            java.lang.Thread.sleep(timeout);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    //TEST NETWORK CONNECTION
    public static boolean testNet() {
        boolean NET = true;
        try {
            Runtime r = Runtime.getRuntime();
            //RUN_COMMAND_AS_ROOT (but only the command not the bot)
            String[] cmd = { "/usr/bin/sudo", "/bin/sh", "-c", "netstat -planet | grep -iw "+PID+"/java | grep -i established" }; //WORKS //remove ".."
            Process p = r.exec(cmd);
            //WAIT_FOR_CMD
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                System.out.println("CHECK_NET [FAILED]: ");
                e.printStackTrace();
                System.exit(-1);
            }
            //OUTPUT
            BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
            if (b.readLine() != null) {
                String line = "";
                while ((line = b.readLine()) != null) {
                    System.out.println(line);
                }
                NET=true;
            } else {
                //System.exit(-1);//DIE?
                //RESTART_CONNECTION
                NET=false;
            }
            b.close();
        } catch (IOException e) {
            System.out.println("[testNet] exception: " +e);
            //e.printStackTrace();
            System.exit(-1);//DIE
        }
        return NET;
    }

    /*** [ THREAD ] KEEP_ALIVE ***/
    //KEEP_ALIVE_TREAD
    public static class threadKeepAlive implements Runnable {
        private Thread t;
        private String threadName;
        private SlackletService _slackService;
        private String _channelName;
        //
        threadKeepAlive (String name, SlackletService slackService, String channelName) {
            threadName = "KeepAlive_t_"+name;
            _slackService = slackService;
            _channelName = channelName;
            System.out.println("Creating " +  threadName );
        }
        //
        public void run() {
            System.out.println("Running " +  threadName );
            try {
                //THREAD_FUNCTION
                keepAliveLoop(_slackService, _channelName);
            } catch (Exception e) {
                System.out.println("Thread " +  threadName + " interrupted." +e);
                run();  //RESTART_DONE_FAIL
            }
            System.out.println("Thread " +  threadName + " exiting.");
        }
        //
        public void start () {
            System.out.println("Starting " +  threadName );
            if (t == null) {
                t = new Thread (this, threadName);
                t.start ();
            }
        }
    }
    //KEEP_ALIVE_FUNCION
    public static void keepAlive(SlackletService slackService, String channelName) {
        if (SLEEP) {
            //slackService.sendMessageTo(channelName, "Zzzz");
            System.out.println("Zzzz");
            if (!testNet()) {
                System.out.println("RESTARTING: Listener");
                startSlackBot();
                System.out.println("STARTED: Listener");
                slackService.sendMessageTo(channelName, "reseting connection.....");
            }
            //RUN_AUTO_TEST_WHEN_IDLE
            autoTest();
        } else {
            //slackService.sendMessageTo(channelName, "Starting sleep cycle......");
            System.out.println("Starting sleep cycle......");
            SLEEP = true;
        }
    }

    //KEEP_ALIVE_FUNCION_LOOP
    public static void keepAliveLoop(SlackletService slackService, String channelName) {
        if (BORED == 0) {
            BORED = System.currentTimeMillis(); //KEEP_ALIVE_TIMESTAMP_INIT_LOOP
        }
        while (true) {
            //(300000) 5 min in milliseconds / 1 * 60000 (1 min)
            if ((System.currentTimeMillis() - BORED) > 300000 ) {
                keepAlive(slackService,channelName);
                BORED = System.currentTimeMillis();
            }
        }
    }

    //RESET KEEP ALIVE
    public static void resetKeepAlive() {
        BORED = System.currentTimeMillis(); //KEEP_ALIVE_RESET (Put This In every Responce) [Or in the first Match]
        SLEEP = false;
    }


    /***********************************************************************
    WHERE THE "MAGIC" HAPPENS
    ************************************************************************/
    //LISTENER
    public static Slacklet slackListener = new Slacklet() {
        @Override
        public void onMessagePosted(SlackletRequest req, SlackletResponse resp) {
            // user posted message and BOT intercepted it
            // get message content
            String content = markdown(req.getContent());
            // reply to the user
            if (content.matches(("(?:^test.*)"))) {
                //DO_TESTS
                File folder = new File("webtest");
                String[] files = folder.list();
                //boolean NOPE = false;
                boolean FOUND = false;
                boolean FOUND2 = false;
                for (String file : files) {
                    if (file.matches(("(?:.*.properties)"))) {
                        String[] testname = file.split("\\.properties");
                        //System.out.println("FILE FOUND: "+file);
                        System.out.println("TESTNAME: "+testname[0]);
                        if (content.matches(("(?:^test\\s*"+testname[0]+"\\s*)"))) {
                            resetKeepAlive(); //RESET_KEEP_ALIVE
                            System.out.println("WebTest: "+testname[0]);
                            resp.reply("Ok testing "+testname[0]+"; please hold... \n(Have Patience) Standard Slack rate limits apply:\n https://api.slack.com/docs/rate-limits");
                            String[] TEST = new String [] {"webtest/"+testname[0]+".properties"};
                            WebTest.main(TEST);
                            resp.reply("Done with "+"@"+testname[0]+"@"+" test");
                            FOUND = true;
                            FOUND2 = true;
                            break;
                        } else {
                            FOUND = false;
                        }
                    }
                }
                if ((!FOUND)&&(!FOUND2)) {
                    String[] split = content.split("\\s+");
                    System.out.println("sorry, I don't know how to test: "+ split[1]);
                    resp.reply("sorry, I don't know how to test: "+ split[1]);
                }

            } else if (content.matches(("(?:^...)"))) {
                //LIST_TESTS
                //String[] testList;
                List<String> testList  = new ArrayList<String>();
                File folder = new File("webtest");
                String[] files = folder.list();
                for (String file : files) {
                    if (file.matches(("(?:.*.properties)"))) {
                        String[] testname = file.split("\\.properties");
                        String ADD = "@"+testname[0]+"@";
                        testList.add(ADD);
                    }
                }
                resp.reply("KNOWN TEST LIST: "+testList); //PRINT_TESTS
            } else if (content.matches(("(?:ping.*)"))) {
                System.out.printf("%s matches%n", content);
                resp.reply("pong");
            } else if (content.matches(("(?:pong.*)"))) {
                System.out.printf("%s matches%n", content);
                resp.reply("ping");
            } else if (content.matches(("(?:do i look fat in this dress\\?.*)"))) {
                System.out.printf("%s matches%n", content);
                resp.reply("Yes, that and any dress....");
            } else if (content.matches(("(?:batman.*)"))) {
                System.out.printf("%s matches%n", content);
                resp.reply("The Lego Batman Movie - Who's the (Bat)Man (Lyrics) \nhttps://www.youtube.com/watch?v=L9ScWnmjpMk \nhttps://www.youtube.com/results?search_query=The+Lego+Batman+Movie+-+Who%27s+the+%28Bat%29Man+%28Lyrics%29");
            } else {
                //if (content.matches(("(?:test.*)"))) {
                //String[] split = content.split("\\s+");
                //System.out.println("sorry, I don't know how to test: "+ split[1]);
                //resp.reply("sorry, I don't know how to test: "+ split[1]);
                //} else {
                System.out.printf("%s does not match%n", content);
                //}
            }
        }
    }; //Need an end to the statment as this is a block-and-statment for [Slacklet slackListener = new Slacklet(){};]
}
//EOF_PUBLIC_SLACK_BOT_CLASS


//WEBTEST_CLASS
class WebTest {
    //GLOBAL_VAR
    public static int count = 0;
    public static StringBuilder URLS = new StringBuilder();
    public static String CONN = null;
    //
    public static int actor_count = 0;
    //
    public static String URL;
    public static String URL_BACK;
    public static String ENDPOINT;
    public static String LAST;
    public static String LAST_TYPE;
    //
    public static String USERNAME;
    public static String PASSWORD;
    //
    public static String key;
    //
    public static String LOC = null;
    public static String IPUB = null;
    //
    public static String WEBTEST;


    /***** SUB CLASS MAIN *****/
    //MAIN
    public static boolean main(String[] args) {
        //eHandler VARS
        String getTest = args[0];
        String[] testname = getTest.split("\\.properties");
        WEBTEST = testname[0];
        // MAIN_THEAD_WITH_eHandler
        Thread.setDefaultUncaughtExceptionHandler(new eHandler()); //USE_UncaughtExceptionHandler

        //INIT_SESSION_FILE
        FileTruncate("send.out"); //RESET_FILE_TO_ZERO

        String RUN = null;

        String DRIVER = null;
        String U1 = null;
        String U2 = null;
        String U3 = null;
        String U4 = null;
        String ELAPSE = null;

        //TIME_STAMP
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss z");
        String strDate = formatter.format(date);
        System.out.println("STARTUP: " + strDate);
        usingFileOutputStream("STARTUP::  " + strDate + "\n");

        //SLACK_INFO_STRING
        RUN = "[WEBTEST (beta)] STARTUP: " + strDate;

        //GET IP & HOSTNAME
        getHostIpInfo();

        //PUBLIC_IP_ADDR
        getPubIpInfo();

        //CHROME
        System.out.println("STARTING DRIVER: ");
        //SLACK_INFO_STRING
        DRIVER = "[WEBTEST] STARTING DRIVER";

        System.setProperty("webdriver.chrome.driver", "chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("-incognito");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-notifications"); //Stop Alerts
        options.addArguments("--disable-popup-blocking"); //Stop Alerts?
        options.addArguments("headless"); //Set Chrome Headless mode
        //options.addArguments("--disable-gpu"); //NO_GPU
        //options.addArguments("--no-sandbox"); //NO_SANDBOX
        //options.addArguments("window-size=1920x1080");
        //options.addArguments("window-size=1280x720");
        options.addArguments("window-size=1024x768");
        //options.addArguments("window-size=800x600");
        //options.addArguments("window-size=640x480");

        //Instantiate Web Driver
        WebDriver driver = new ChromeDriver(options);
        //HtmlUnitDriver driver = new HtmlUnitDriver();
        //driver.setJavascriptEnabled(true);

        //DISABLE_JSCRIPT_POPUP
        String JS_DISABLE_UNLOAD_DIALOG = "Object.defineProperty(BeforeUnloadEvent.prototype, 'returnValue', { get:function(){}, set:function(){} });";
        //(JavascriptExecutor) driver).executeScript(JS_DISABLE_UNLOAD_DIALOG);
        JavascriptExecutor jse = (JavascriptExecutor)driver;
        jse.executeScript(JS_DISABLE_UNLOAD_DIALOG);

        //RUN_TIME_START
        long start = System.currentTimeMillis();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        //driver.get("chrome://settings/advanced");
        //driver.findElement(By.name("privacyContentSettingsButton")).click();
        //driver.findElement(By.name("popups")).click();

        //CLASS_HANDLING_THE_EXCEPTION_HERE
        try {
            slack("STARTING UP TEST ["+WEBTEST+"]....");
            //RUN_WEB_TEST_FROM_PROP_FILE
            runWebTestFromFile(driver, args);
        } catch (Exception e) {
            //ERROR_VARS
            String LOGIN = "[WEBTEST] TEST END POINT: " + "@" + ENDPOINT + "@" + "\n[WEBTEST] LOGIN INFO: USERNAME = " + USERNAME + " / PASSWORD = " + PASSWORD;
            System.out.println("[ Failed@"+WEBTEST+" ]: \n"+ RUN + "\n" + LOGIN + "\n" + LOC + "\n" + IPUB + "\n" + DRIVER + "\n" + CONN + "\n" + URLS + ELAPSE + "\n WebTest Error: " + e);
            slack("[ Failed@"+WEBTEST+" ]: \n"+ RUN + "\n" + LOGIN + "\n" + LOC + "\n" + IPUB + "\n" + DRIVER + "\n" + CONN + "\n" + URLS + ELAPSE + "\n WebTest Error: " + e);
            //SEND IMAGE AT FAILURE LOCATION
            urlCap(driver);
            makeWebImage();
            sendImageToSlack();
            driver.quit(); //REMOVE DRIVER FROM ME
            //CLEANUP
            command("pkill -9 chromedriver");
            command("/usr/bin/rm screenshot*.png");
            command("/usr/bin/rm screenshot*.gif");
            URLS.setLength(0);
            count = 0;
            actor_count = 0;
            USERNAME = "";
            PASSWORD = "";
            return false;
        }

        //RUN_TIME_END
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("RUN_TIME (milliseconds): " + timeElapsed);
        usingFileOutputStream("RUN_TIME (milliseconds): " + timeElapsed + "\n");
        //SLACK_INFO_STRING
        ELAPSE = "[WEBTEST] TEST_RUN_TIME (milliseconds): " + timeElapsed;

        //SEND_TEXT_INFO_ONE_CALL
        System.out.println("Sending Text to Slack..");
        String LOGIN = "[WEBTEST] TEST END POINT: " + "@" + ENDPOINT + "@" + "\n [WEBTEST] LOGIN INFO: USERNAME = " + USERNAME + " / PASSWORD = " + PASSWORD;
        slack("[ Passed@"+WEBTEST+" ]: \n" + RUN + "\n" + LOGIN + "\n" + LOC + "\n" + IPUB + "\n" + DRIVER + "\n" + CONN + "\n" + URLS + ELAPSE + "\n[WEBTEST] OK\n");

        //MAKE_IMAGE
        makeWebImage();
        //SEND_IMAGE
        sendImageToSlack();

        System.out.println("Done..");
        //System.out.println(URLS);

        //driver.Close(); //Close the browser window that the driver has focus of
        driver.quit(); //REMOVE DRIVER FROM MEM

        //CLEANUP
        command("pkill -9 chromedriver");
        command("/usr/bin/rm screenshot*.png");
        command("/usr/bin/rm screenshot*.gif");
        URLS.setLength(0);
        count = 0;
        actor_count = 0;
        USERNAME = "";
        PASSWORD = "";
        return true;
    }//END_OF_MAIN

    /***** UNKNOWN EXCEPTIONS *****/

    // DO_SOMETHING_WITH_AN_UNCAUGHT_EXCEPTION
    private static final class eHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            System.out.printf("An UNCAUGHT exception has been captured\n");
            System.out.printf("Thread: %s\n", t.getId());
            System.out.printf("Exception: %s: %s\n", e.getClass().getName(), e.getMessage());
            System.out.printf("Stack Trace: \n");
            e.printStackTrace(System.out);
            System.out.printf("Thread status: %s\n", t.getState());
            //CLEANUP
            //NOTE: SEND IMG OF FAIL LOCATION; REMOVE IMG; TAKE SNAPSHOT; SEND IMAGE WITH ERROR
            //TRY_FIND_AND_KILL: ALL DRIVERS AND BROWSERS ASSOCIATED WITH THIS PROC
            command("pkill -9 chromedriver");
            command("/usr/bin/rm screenshot*.png");
            command("/usr/bin/rm screenshot*.gif");
            URLS.setLength(0);
            count = 0;
            actor_count = 0;
            USERNAME = "";
            PASSWORD = "";
            // Terminate JVM
            //System.exit(-1);
            slack("[[ UNCAUGHT exception ]] WebTest Error["+WEBTEST+"]:" + e );
        }
    }

    /***** CLASS FUNCTIONS *****/

    //[ACTION] WAIT
    //threadWait
    public static void threadWait(int timeout) {
        try {
            // thread to sleep for 1 sec = 1000 milliseconds
            java.lang.Thread.sleep(timeout);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    //[ACTION] GET LOCAL IP
    public static void getHostIpInfo() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            //System.out.println("IP Address: " + inetAddress.getHostAddress());
            //System.out.println("Host Name: " + inetAddress.getHostName());
            System.out.println("TEST_LOCATION: " + inetAddress.getHostName() + " / " + inetAddress.getHostAddress());
            usingFileOutputStream("TEST_LOCATION: " + inetAddress.getHostName() + " / " + inetAddress.getHostAddress() + "\n");
            //SLACK_INFO_STRING
            LOC = "[WEBTEST] TEST_LOCATION: " + inetAddress.getHostName() + " / " + inetAddress.getHostAddress();

        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }

    //[ACTION] GET PUB IP
    public static void getPubIpInfo() {
        try {
            URL url_name = new URL("http://bot.whatismyipaddress.com");
            BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
            String PUBIP = sc.readLine().trim();
            System.out.println("PUBLIC_IP_ADDR: " + PUBIP);
            usingFileOutputStream("PUBLIC_IP_ADDR: " + PUBIP + "\n");
            //SLACK_INFO_STRING
            //TURNOFF// IPUB = "";
            IPUB = "[WEBTEST] PUBLIC_IP_ADDR: " + PUBIP;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //[ACTION] RUN_SHELL_COMMANDS
    public static void command(String run) {
        try {
            Runtime r = Runtime.getRuntime();
            //USE_SHELL_INTERPRETER
            String[] cmd = { "/bin/sh", "-c", run };//WORKS
            Process p = r.exec(cmd);

            try {
                p.waitFor();
            } catch (InterruptedException e) {
                System.out.println("exception: ");
                e.printStackTrace();
                System.exit(-1);
            }

            BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = b.readLine()) != null) {
                System.out.println(line);
            }
            b.close();
        } catch (IOException e) {
            System.out.println("exception: ");
            e.printStackTrace();
            System.exit(-1);
        }
    }


    //[ACTION] BUILD GIF IMAGE
    public static void makeWebImage() {
        System.out.println("Making Image...");
        //convert -loop 0 -delay 200 screenshot1.png screenshot2.png screenshot3.png screenshot.gif
        try {
            Runtime r = Runtime.getRuntime();
            //Process p = r.exec(content.substring(2));//remove ".."
            //USE_SHELL_INTERPRETER
            String[] cmd = { "/bin/sh", "-c", "convert -loop 0 -delay 200 screenshot*.png screenshot.gif" };//WORKS //remove ".."
            Process p = r.exec(cmd);
            //
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                System.out.println("exception: ");
                e.printStackTrace();
                System.exit(-1);
            }
            //OUT
            BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String error = "";
            while ((error = b.readLine()) != null) {
                System.out.println(error);
                //resp.reply("```"+error+"```");
            }
            b.close();
        } catch (IOException e) {
            System.out.println("exception: ");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void sendImageToSlack() {
        //SEND_SCREEN_SHOTS
        System.out.println("Sending Images to Slack...");
        command("slack-cli -t "+OnDemandHealthCheck.botToken+" -d "+OnDemandHealthCheck.channelName+" -f screenshot.gif");
    }

    //[ACTION] WRITE_TO_FILE
    public static void usingFileOutputStream(String textToAppend) {
        try {
            FileOutputStream outputStream = new FileOutputStream("send.out", true);
            byte[] strToBytes = textToAppend.getBytes();
            outputStream.write(strToBytes);
            outputStream.close();
        } catch(IOException e) {
            System.err.println("Caught IOException: " +  e.getMessage());
        }
    }

    //[ACTION] INIT_FILE
    public static void FileTruncate(String filename) {
        File file = new File(filename); //RESET_FILE
        try (FileOutputStream fos = new FileOutputStream(file)) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //[ACTION] TEXT_TO_SLACK
    public static void slack(String say) {
        String botToken = OnDemandHealthCheck.botToken;
        String channelName = OnDemandHealthCheck.channelName;
        //CONNECT_TO_SLACK
        try {
            SlackletService slackService = new SlackletService(botToken);
            slackService.start();
            slackService.sendMessageTo(channelName, say); //SENDMSG
            slackService.stop();
            System.out.println("[[ slack ]]: Done");
        } catch (ConnectException e) {
            System.out.println("[[ ERROR_FIXUP@slack ]]  Caught ConnectException: " +  e);
            threadWait(4000);
            slack(say);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //[ACTION] DISPLAY URLINFO AND SCREENSHOT
    public static String urlCap(WebDriver driver) {
        count++;
        //PRINT_URL
        String urls = driver.getCurrentUrl();
        System.out.println("URL: " + urls);
        usingFileOutputStream("URL: " + urls + "\n");
        //RETURN_URL_INFO
        String URLINFO = "[WEBTEST] URL: " + "@"+urls+"@";
        //GET SCREENSHOT
        File src = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        try {
            //FileUtils.copyFile(src, new File("screenshot" + count + ".png"));
            copyFileUsingStream(src, new File("screenshot" + count + ".png"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        URLS.append(URLINFO + "\n");
        URL_BACK = URLINFO; //??
        return URLINFO;
    }

    //FILE_COPY
    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    //[ACTION] WAIT_FOR_ELEMENT
    public static void actionWait(WebDriver driver, String INPUT) throws Exception {
        //LET_THE_CLASS_HANDLE_THE_EXCEPTION
        //try {
        WebElement DynamicElement = (new WebDriverWait(driver, 10)).until(ExpectedConditions.visibilityOfElementLocated(elementBy(key, INPUT)));
        //} catch (NoSuchElementException ignored) {
        //return false;
        //} catch (StaleElementReferenceException ignored) {
        //return false;
        //} catch (Exception e) {
        //System.out.println("TIMEOUT @ " + INPUT);
        //return false;
        //}
    }

    //SET_TYPE (STRING)
    public static String mtype (String mdata) {
        String RTYPE;
        if ( key.matches("(?:.*xpath.*)") || mdata.matches("(?:^//.*)") || mdata.matches("(?:^.//.*)") || mdata.matches("(?:^/.*/.*)") || mdata.matches("(?:^\\(.//.*)")) {
            //System.out.println("matches XPATH " + mdata);
            RTYPE = "By.xpath";
        } else if ( key.matches("(?:.*cssSelector.*)") || mdata.matches("(?:^#.*)") || mdata.matches("(?:^div\\..*)") || mdata.matches("(?:^[a-z].*\\[.*\\])") || mdata.matches("(?:^[a-z].*:.*[a-z]\\([0-9].*\\))") || mdata.matches("(?:^[a-z].*#.*[a-z].*)") || mdata.matches("(?:^.*[a-z].*#.*[a-z].*)") ) {
            RTYPE = "By.cssSelector";
            //System.out.println("matches cssSelector " + mdata);
        } else if ( key.matches("(?:.*tag.*)") || mdata.matches("(?:\\<.*)") ) {
            RTYPE = "By.tagName";
            //System.out.println("matches tagName " + mdata);
        } else if ( key.matches("(?:.*id.*)") ) {
            RTYPE = "By.id";
            //System.out.println("matches ID " + mdata);
        } else if ( key.matches("(?:.*name.*)") ) {
            RTYPE = "By.name";
            //System.out.println("matches Name " + mdata);
        } else if ( key.matches("(?:.*classname.*)") ) {
            RTYPE = "By.className";
            //System.out.println("matches Classname " + mdata);
        } else {
            //RTYPE = By.linkText(mdata);
            RTYPE = "By.LinkText";
            //System.out.println("matches linkText " + mdata);
        }
        return RTYPE;
    }


    //SET_ELEMENT_TYPE (Object: By)
    public static By elementBy(String key, String mdata) {
        By RTYPE;
        if ( key.matches("(?:.*xpath.*)") || mdata.matches("(?:^//.*)") || mdata.matches("(?:^.//.*)") || mdata.matches("(?:^/.*/.*)") || mdata.matches("(?:^\\(.//.*)")) {
            //System.out.println("matches XPATH " + mdata);
            RTYPE = By.xpath(mdata);
        } else if ( key.matches("(?:.*cssSelector.*)") || mdata.matches("(?:^#.*)") || mdata.matches("(?:^div\\..*)") || mdata.matches("(?:^[a-z].*\\[.*\\])") || mdata.matches("(?:^[a-z].*:.*[a-z]\\([0-9].*\\))") || mdata.matches("(?:^[a-z].*#.*[a-z].*)") || mdata.matches("(?:^.*[a-z].*#.*[a-z].*)") ) {
            RTYPE = By.cssSelector(mdata);
            //System.out.println("matches cssSelector " + mdata);
        } else if ( key.matches("(?:.*tag.*)") || mdata.matches("(?:\\<.*)") ) {
            RTYPE = By.tagName(mdata);
            //System.out.println("matches tagName " + mdata);
        } else if ( key.matches("(?:.*id.*)") ) {
            RTYPE = By.id(mdata);
            //System.out.println("matches ID " + mdata);
        } else if ( key.matches("(?:.*name.*)") ) {
            RTYPE = By.name(mdata);
            //System.out.println("matches Name " + mdata);
        } else if ( key.matches("(?:.*classname.*)") ) {
            RTYPE = By.className(mdata);
            //System.out.println("matches Classname " + mdata);
        } else {
            //RTYPE = By.linkText(mdata);
            RTYPE = By.partialLinkText(mdata);
            //System.out.println("matches linkText " + mdata);
        }
        return RTYPE;
    }

    //ACTOR (Choose Action)
    public static void actor(WebDriver driver, String TYPE, String ACTION, String INPUT, String DATA) throws Exception {
        //LET_THE_CLASS_HANDLE_THE_EXCEPTION
        //System.out.println(driver.getCurrentUrl()+":"+TYPE+":"+ACTION+":"+DATA);
        System.out.println("RUN_INPUT_FOR_ACTOR: " + TYPE + ":" + ACTION + ":" + INPUT + ":" + DATA);
        //
        if ( ACTION.matches("(?:.*.thr.*)") ) {
            System.out.println("THREAD_WAIT: " + TYPE + ":" + ACTION + ":" + INPUT + ":" + DATA);
                    int WaitTime = java.lang.Integer.parseInt(ACTION);
                    threadWait(WaitTime);
                    //SCREEN_SHOT
                    urlCap(driver);
        } else if ( ACTION.matches("(?:.*click.*)") ) { //CLICK
            System.out.println("RUN_CLICK_AS_ACTOR: " + TYPE + ":" + ACTION + ":" + INPUT + ":" + DATA);
            try{
                driver.findElement(elementBy(key, INPUT)).click();
            } catch (Exception e) {
                /** ERROR:  ElementClickInterceptedException
                    WebTest Error: org.openqa.selenium.WebDriverException: element click intercepted: Element <span class="ng-binding">...</span> 
                    is not clickable at point (443, 345). Other element would receive the click: <h6>...</h6>
                ***/
                System.out.println("[[ ERROR_FIXUP@CLICK ]] webDriver failed to click trying Jscript - Click: " + TYPE + ":" + ACTION + ":" + INPUT + ":" + DATA);
                String ex = e.toString();
                String[] exArr = ex.split(":");
                String OK = exArr[1];
                System.out.println("[[ WHAT ]]: "+ exArr[1]);
                System.out.println("[TEST?]:"+ ex.matches("(?:.*element.*click.*intercepted.*)")); //WILL_NOT_MATCH[NEED TO SPLIT]
                System.out.println("[TEST?]:"+ OK.matches("(?:.*intercepted.*)"));
                System.out.println("[TEST?]:"+ OK.matches("(?:.*element.*click.*intercepted.*)"));
                System.out.println("[[ WHAT FAILED ]]: "+ ex);
                //IF [ element.*click.*intercepted ]
                boolean isError  = OK.matches("(?:.*element.*click.*intercepted.*)");
                if (isError){
                    JavascriptExecutor jsClick = (JavascriptExecutor)driver;
                    jsClick.executeScript("arguments[0].click();", driver.findElement(elementBy(key, INPUT)));
                } else {
                    throw (e);
                }
            }
        } else if ( ACTION.matches("(?:.*sendkey.*)") ) { //SEND KEYS
            System.out.println("RUN_SENDKEYS_AS_ACTOR: " + TYPE + ":" + ACTION + ":" + INPUT + ":" + DATA);
            //driver.findElement(elementBy(key, INPUT)).sendKeys(DATA);
            //NEED_TO_USE_THE_CACHED_ELEMENT_FOR_THIS_ACTION
            driver.findElement(elementBy(LAST_TYPE, INPUT)).sendKeys(DATA);
        } else if ( ACTION.matches("(?:.*\\.wait.*)") ) { //WAIT
            System.out.println("RUN_WAIT_AS_ACTOR: " + TYPE + ":" + ACTION + ":" + INPUT + ":" + DATA);
            actionWait(driver, INPUT); //WAIT_FOR_ELEMENT
        } else if ( ACTION.matches("(?:.*Navigate.*)") ) { //NAV
            System.out.println("RUN_NAV_AS_ACTOR: " + TYPE + ":" + ACTION + ":" + INPUT + ":" + DATA);
            driver.navigate().to(INPUT);
        } else if ( ACTION.matches("(?:.*back.*)") ) { //BACK
            System.out.println("RUN_BWD_AS_ACTOR: " + TYPE + ":" + ACTION + ":" + INPUT + ":" + DATA);
            //driver.navigate().to(URL_BACK);
            driver.navigate().back();
            driver.navigate().refresh();
            System.out.println("BACK TO THE LAST URL" );
        }
    }//END_ACTOR


    //RUN_WEB_TEST_FROM_PROP_FILE
    public static void runWebTestFromFile(WebDriver driver, String[] args) throws Exception {
        //LET_THE_CLASS_HANDLE_THE_EXCEPTION
        //NOTE: USE_ACTOR_START_HERE
        //READ_PROP_FILE
        String PROP_FILE;
        if (args.length == 0) {
            PROP_FILE = "config.properties";
        } else {
            PROP_FILE = args[0];
        }
        System.out.println("*** PROP_FILE:" + PROP_FILE);

        try (InputStream input = new FileInputStream(PROP_FILE)) {
            //load a properties file
            Properties prop = new Properties();
            prop.load(input);
            //MAKE_HASH
            HashMap<String, String> propvals = new HashMap<String, String>();
            Set<String> propertyNames = prop.stringPropertyNames();
            //
            for (String Property : propertyNames) {
                //System.out.println(Property + ":" + prop.getProperty(Property));
                propvals.put(Property, prop.getProperty(Property));
            }
            //
            //propvals.put("key", "value");
            TreeMap<String, String> sorted = new TreeMap<>();
            sorted.putAll(propvals);
            //
            for(Map.Entry<String, String> entry : sorted.entrySet()) {
                //String key = entry.getKey();
                key = entry.getKey();
                String value = entry.getValue();
                System.out.println("\nDATA: " + key + ":" + value + "\t [TYPE: " + mtype(prop.getProperty(key)) + "]");
                if (value.matches("(?:http.//.*)") || value.matches("(?:https.//.*)")) {
                    String read = prop.getProperty(key).toLowerCase();
                    System.out.println("URL: " + read);
                    System.out.println("ACTION GOTO PAGE");
                    if (count == 0) {
                        ENDPOINT = read;
                    }
                    //ACTOR
                    actor(driver, "", "Navigate", read, "");
                    System.out.println("CONNECTING TO URL");
                    usingFileOutputStream("CONNECTING TO URL" + "\n");
                    //SLACK_INFO_STRING
                    CONN = "[WEBTEST] CONNECTING TO URL";
                } else if (key.matches("(?:.*thr.*)") ) {
                    String read = prop.getProperty(key);
                    int WaitTime = java.lang.Integer.parseInt(read);
                    threadWait(WaitTime);
                    //SCREEN_SHOT
                    urlCap(driver);
                    actor(driver, "", "threadwait", read, "");
                } else if ( key.matches("(?:.*back.*)") ) {
                    System.out.println("ACTION GO BACK: ");
                    //ACTOR
                    actor(driver, "", "back", "", "");
                } else if ( key.matches("(?:.*\\.wait.*)") ) {
                    String read = prop.getProperty(key);
                    System.out.println("ACTION WAIT FOR: " + mtype(read) + "::" + read);
                    //ACTOR
                    actor(driver, mtype(read), "waitfor", read, "");
                } else if ( ( !(key.matches("(?:.*click.*)")) && !(key.matches("(?:.*sendkeys.*)")) ) ) {
                    //NO_DEFAULT_ACTION_CACHE_ELEMENT_FOR_LATER_ACTION
                    String read = prop.getProperty(key);
                    System.out.println("INPUT: " + read);
                    System.out.println("INPUT(TYPE): " + mtype(read));
                    LAST = read; //SET_ELEMENT
                    LAST_TYPE = mtype(read);
                    System.out.println("ACTION CACHE DATA: " + LAST_TYPE + "::" + LAST);
                } else if (key.matches("(?:.*sendkeys.*)") ) {
                    String read = prop.getProperty(key);
                    System.out.println("SEND KEYS: " + read);
                    System.out.println("ACTION SEND KEYS TO: " + LAST_TYPE + "::" + LAST);
                    //ACTOR
                    actor(driver, LAST_TYPE, "sendkeys", LAST, read);
                    //DUMB_DATA
                    if (actor_count == 3) {//WAS 3
                        actor_count = 0;
                    }
                    if (actor_count == 0) {
                        USERNAME = read;
                        actor_count++;
                        System.out.println("TRY LOGIN: " + USERNAME + " / " + PASSWORD);
                    } else if (actor_count == 1) {
                        PASSWORD = read;
                        actor_count++;
                    }
                } else if (key.matches("(?:.*click.*)") ) {
                    String read = prop.getProperty(key);
                    if(read.length() == 0) {
                        System.out.println("ACTION CLICK: " + LAST_TYPE + "::" + LAST);
                        //ACTOR
                        actor(driver, LAST_TYPE, "click", LAST, "");
                    } else {
                        System.out.println("ACTION CLICK: " + mtype(read) + "::" + read);
                        //ACTOR
                        actor(driver, mtype(read), "click", read, "");
                    }
                } else {
                    System.out.println(" ..... ");
                }
            }//END_OF_LOOP
        } catch (IOException ex) {
            ex.printStackTrace();
        }//END_TRY
    }//END_OF_RUN_WEB_TEST_FROM_PROP_FILE


}//END_OF_WEBTEST_CLASS

//EOF


