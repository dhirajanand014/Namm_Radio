package aegismatrix.com.namm_radio;

import java.util.Map;

/**
 * Created by Dhiraj on 08-04-2017.
 */

public final class GlobalConstants {
    public static final String PROGRAM_SCHEDULES_JSON_OBJECT = "programSchedules";
    public static final String STATION_LINKS_JSON_OBJECT = "stationLinks";
    public static final String PROGRAM_SCHEDULES_JSON_PROGRAM_NAME = "programName";
    public static final String PROGRAM_SCHEDULES_JSON_HOST_NAME = "hostName";
    public static final String PROGRAM_IMAGE_TAG = "image";
    public static final String PROGRAM_SCHEDULES_JSON_PROGRAM_TIMINGS = "programTimings";
    public static final String APP_BACKGROUND_IMAGE = "http://temp2.aegismatrix.com/Background.jpg";
    public static final String PROGRAM_SCHEDULE_CURENT_PROGRAM_EQUILISER = "http://temp2.aegismatrix.com/ps/equlizer.png";
    public static final String[] STATIONS = {"INDIA", "USA", "GULF"};
    public static final String[] GULF_CONTINENTS = {"KW", "OM", "QA", "SA", "AE", "YE", "BH", "IQ"};
    public static final int PERMISION_REQUEAT_CODE = 11;
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 7172;
    public static final int UPDATE_INTERVAL = 5000; // SEC
    public static final int FASTEST_INTERVAL = 3000; // SEC
    public static final int DISPLACEMENT = 10; //METERS
    public static final String RECEIVER = "NammRadioSplashScreen.RECEIVER";
    public static final String LOCATION_DATA_EXTRA = "NammRadioSplashScreen.LOCATION_DATA";
    public static final String RESULT_DATA_KEY = "NammRadioSplashScreen.RESULT_DATA_KEY";
    public static final int SUCCESS_RESULT = 1;
    public static final int FAILURE_RESULT = 0;
    public static Map<String, String> STATIONLINKS;
}
