package DB;

public class Const {
    // Table cash registers
    public static final String REGISTERS_TABLE = "cash_registers";
    public static final String REGISTERS_ID = "id_register";
    public static final String REGISTERS_TYPE = "type";
    // Table admissions
    public static final String ADMISSIONS_TABLE = "admissions";

    public static final String ADMISSIONS_ID = "id_admission";
    public static final String ADMISSIONS_CINEMA = "cinema_id";
    public static final String ADMISSIONS_SESSION = "session_id";
    public static final String ADMISSIONS_REGISTER = "cashregister_id";
    public static final String ADMISSIONS_SOLD = "sold";
    public static final String ADMISSIONS_TOTAL = "total";
    // Table cinemas
    public static final String CINEMAS_TABLE = "cinemas";
    public static final String CINEMAS_ID = "id_cinema";
    public static final String CINEMAS_NAME = "name";
    // Table movies
    public static final String MOVIES_TABLE = "movies";
    public static final String MOVIES_ID = "id_movie";
    public static final String MOVIES_TITLE = "title";
    // Table sessions
    public static final String SESSIONS_TABLE = "sessions";

    public static final String SESSIONS_ID = "id_session";
    public static final String SESSIONS_CINEMA = "cinema_id";
    public static final String SESSIONS_MOVIE = "movie_id";
    public static final String SESSIONS_DATE = "date";
    public static final String SESSIONS_TIME = "time";
}