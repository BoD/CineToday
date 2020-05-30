import ca.rmen.lfrc.FrenchRevolutionaryCalendar
import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.Properties
import java.util.TimeZone

/**
 * Returns a file on the project's root - creates it from a sample if it doesn't exist.
 */
fun Project.getOrCreateFile(fileName: String): File {
    val res = file(fileName)
    if (!res.exists()) {
        logger.warn("$fileName file does not exist: creating it now - please check its values")
        copy {
            from("${fileName}.SAMPLE")
            into(project.projectDir)
            rename { fileName }
        }
    }
    return res
}

/**
 * Get a Map<String, String> loaded from properties file.
 */
fun MutableMap<String, String>.loadFromFile(file: File) {
    val properties = Properties()
    val fileInputStream = FileInputStream(file)
    fileInputStream.use {
        properties.load(it)
    }
    for ((k, v) in properties) {
        put(k.toString(), v.toString())
    }
}

private fun exec(vararg command: String): String = ProcessBuilder(*command)
    .start()
    .inputStream
    .bufferedReader()
    .readLine()

/**
 * Returns the SHA1 of the current git commit.
 */
fun Project.getGitSha1(): String = exec("git", "--git-dir=${rootDir}/.git", "--work-tree=${rootDir}", "rev-parse", "--short", "HEAD")

/**
 * Returns the name of the current git branch.
 */
fun Project.getGitBranch(): String = exec("git", "--git-dir=${rootDir}/.git", "--work-tree=${rootDir}", "rev-parse", "--abbrev-ref", "HEAD")

/**
 * The current date/time formatted in UTC.
 */
val buildDate: String by lazy {
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
        .format(Date())
}

/**
 * This is for amusement purposes :)
 */
fun getFrenchDate(): String {
    val frenchDate = FrenchRevolutionaryCalendar(
        Locale.FRENCH,
        FrenchRevolutionaryCalendar.CalculationMethod.ROMME
    ).getDate(GregorianCalendar())
    return "Le ${frenchDate.weekdayName} ${frenchDate.dayOfMonth} ${frenchDate.monthName} de l'an ${frenchDate.year}. (${frenchDate.objectTypeName} du jour : ${frenchDate.objectOfTheDay})"
}


/**
 * Show a "splash screen".
 */
fun Project.printSplashScreen() {
    val ansiPurple = "\u001B[95m"
    val ansiReset = "\u001B[0m"
    println(
        """

            ====================================${ansiPurple}
                 _____  ___   ____
             __ / / _ \/ _ | / __/___  _______ _
            / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
            \___/_/¦_/_/ |_/_/ (_)___/_/  \_, /
                                         /___/
            ${ansiReset}
            Building ${rootProject.name}.
            
            ${getFrenchDate()}
            
            rootDir: $rootDir
            buildDate: $buildDate
            versionCode: ${AppConfig.buildNumber}
            versionName: ${AppConfig.buildProperties["versionName"]}
            gitBranch: ${getGitBranch()}
            gitSha1: ${getGitSha1()}
            
            ====================================
        """.trimIndent()
    )
}