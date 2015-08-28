DATE4J

But, soft! what code in yonder program breaks?
It is the Date, and dim grows the Sun!
Arise, fair Sun, and kill the egregious Date,
Who is already sick and pale with grief,
That we, perchance, art more fair than she.
This site offers a Java tool called date4j. It's an alternative to Date, Calendar, and related Java classes. The JDK's treatment of dates is likely the single most suctorial aspect of the Java core libraries. It needs improvement.

The main goals of date4j are:

easy manipulation of dates/times in the Gregorian calendar (the civil calendar used in almost all countries).
easy storage and retrieval of such dates/times from a relational database.
a simplified model of civil timekeeping, similar to the model used by many databases.
Problem

For reference, here are the JDK classes related to dates.
Date and its subclasses:

java.util.Date
java.sql.Date
java.sql.Timestamp
The calendar and time zone classes:

java.util.Calendar
java.util.GregorianCalendar
java.util.TimeZone
java.util.SimpleTimeZone (for use with the Gregorian calendar only)
The formatting and parsing classes:

java.text.DateFormat
java.text.SimpleDateFormat
java.text.DateFormatSymbols
The problem is that the above classes are widely regarded as being of disturbingly poor quality, for various reasons:
mistakes regarding time zones are very common (see Martin Fowler's comments, for example, and this link).
dates are mutable, but basic 'building block' classes should be immutable. There are many advantages to being an immutable object. Integer, String, BigDecimal, Boolean and so on, are all immutable. Dates should be as well. As stated by Brian Goetz, "[Date's mutability] was probably a mistake in the class library design."
Calendar is too complex. Joshua Bloch states: "As an extreme example of what not to do, consider the case of java.util.Calendar. Very few people understand its state-space -- I certainly don't -- and it's been a constant source of bugs for years."
January is assigned an index of 0, not 1, leading to silly off-by-one errors.
numerous methods use JRE defaults for TimeZone and Locale. In some server environments, multiple applications can share the same JRE. Thus, the default can be changed by one application, and read by another. Such cross-talk is unacceptable.
calculating the number of days between 2 dates isn't as simple as it should be.
Timestamp subclasses Date, and adds a nanoseconds aspect. As stated in Effective Java, that makes it impossible to correctly implement the equals and hashCode methods for Timestamp.
Calendar has two items referring to the hour of the day, HOUR and HOUR_OF_DAY. If, for example, you need to set the time portion of a Calendar object to 0, then you must set both the HOUR and HOUR_OF_DAY.
java.sql.Date is intended to represent a date without a time, but that is not actually enforced.
since rules for time zones and summer hours can change according to the arbitrary whims of legislators, the JRE needs to be updated, on occasion, just to reflect changes to such legislation.
many complain that since SimpleDateFormat is not thread-safe, you cannot share instances between threads.
numerous other minor nuisances as well.
Joda Time

The Joda Time library is used by some programmers as an alternative to the JDK date classes. Although it's a significant improvement over the JDK, Joda Time is not without its own drawbacks:
Joda limits precision to milliseconds. Database timestamp values almost always have a precision of microseconds or even nanoseconds. This is a serious defect: a library should never truncate your data, for any reason.
Joda allows mutable versions of classes.
Joda is large, with well over 100 items in its javadoc. Why does a programmer need 138 classes just to model a date, and perform common operations? Doesn't that seem excessive? (If you use date4j, the number of classes you'll need to model dates in the Gregorian Calendar is exactly 1.)
in order to stay current, Joda needs to be updated occasionally with fresh time zone data.
Joda can be slow on Android, unless steps are taken to avoid unwanted loading of time zone data.
Joda always coerces March 31 + 1 Month to April 30 (for example), without giving you any choice in the matter.
some databases allow invalid date values such as '0000-00-00', but Joda Time doesn't seem to be able to handle them.
How Databases Treat Dates

Most databases model dates and times using the Gregorian Calendar in an aggressively simplified form, in which:
the Gregorian calendar is extended back in time as if it was in use previous to its inception (the 'proleptic' Gregorian calendar)
the transition between Julian and Gregorian calendars is entirely ignored
summer hours are entirely ignored
leap seconds are entirely ignored
often, even time zones are ignored, in the sense that the underlying database column doesn't usually explicitly store any time zone information.
How Databases Treat Time Zones

For storing time zone information, many databases don't include any data types at all. DB2, MySQL, and SQLServer are all in this category. Applications which find it necessary to store explicit time zones will, when using such databases, need to create their own solution. Given the problems noted below, such ad hoc solutions are not necessarily a bad thing.
Some databases, such as Oracle and PostgreSQL, do indeed supply data types explicitly for handling time zones, but the implementations are a mess.

First of all, take the ANSI SQL standard. Its very definition of time zone doesn't match the definition of TimeZone in Java. Why? Because ANSI SQL defines time zones as a fixed offset from Universal Time. But an offset is not a time zone. Since they don't take into account summer hours, they don't match what most people think of as a proper time zone. Such a glaring mismatch of fundamental abstractions is bound to be a fruitful source of error, annoyance, and widespread confusion.

PostgreSQL
PostgreSQL has 2 data types named TIME WITH TIME ZONE and TIMESTAMP WITH TIME ZONE. These columns store time zone/offset information, right? Wrong. Neither a time zone nor an offset is stored in these fields. From their documentation:

"All timezone-aware dates and times are stored internally in UTC. They are converted to local time in the zone specified by the timezone configuration parameter before being displayed to the client."

So, what you have here is a misrepresentation of what is being stored. The name of the data type clearly implies that a time zone/offset is being stored, but that's clearly not the case. There is unequivocally no explicit time zone/offset is stored in these columns. None whatsoever. Rather, an implicit offset is used, and a calculation is applied to the data, using particular policies defined by the database, involving the difference between 2 offsets.

Oracle
Oracle has 2 data types named TIMESTAMP WITH TIME ZONE and TIMESTAMP WITH LOCAL TIME ZONE.

TIMESTAMP WITH TIME ZONE stores an offset in the column. Again, an offset is not the same thing as a time zone.

TIMESTAMP WITH LOCAL TIME ZONE doesn't store explicit offset information at all. Rather, it implicitly uses the database's offset. Again, when returning this data to a client, a database-defined policy is applied (using a difference in offsets), a calculation is performed, and the altered value is returned.

The difference in offsets is usually (but not always) calculated using DBTIMEZONE (the database default) and SESSIONTIMEZONE (the session setting). Again, such policies may or may not be relevant or appropriate for an application.

Oracle does mention the idea of time zones as opposed to offsets (as in 'America/Montreal'), but these items are apparently not stored anywhere in a database column.

Database Offset Calculations 
Clearly, databases that do attempt to manage time zones for you are applying a number of 'baked-in' policies. But such calculations are a major annoyance. Here's why:

the calculations aren't time zone conversions! This is because an offset is not a time zone (see above).
the settings that control the calculation of the offset difference are all over the place. Thus, controlling these settings, and understanding exactly how they interact to produce the result you're seeing, is often a pain in the butt.
the database policies for doing offset difference calculations are just that - certain policies. But there are many applications for which these policies are irrelevant. For example, if you want to calculate an offset difference based on a user preference, then database policies are likely useless.
all other data types other than date-times can be returned from the database as is, without alteration. Why is an annoying exception being made in the case of date-time data?
databases are good at storing data, but it's not clear if a database should even attempt such non-trivial transformations in the first place. Would it not be easier, simpler, and clearer to perform such transformations in the application, not in the database? Where there is access, for example, to the end user's preferences?
Solution

The date4j tool chooses to focus on how databases can store dates and times in a simple style (without time zone/offset), and not on modeling the arcane details of civil timekeeping.
In summary:

its public API consists of a single public class called DateTime. That class is immutable.
it doesn't store any time zone information. Most date-times are stored in columns whose type does not include time zone information (see note above).
it ignores all non-linearities: summer-hours, leap seconds, and the cutover from Julian to Gregorian calendars.
its precision matches the highest precision used by databases (nanosecond).
it uses only the proleptic Gregorian Calendar, over the years 1..9999.
it has (very basic) support for wonky dates, such as the magic value 0000-00-00 used by MySQL.
it lets you choose among 4 policies for 'day overflow' conditions during calculations.
Recommendations for using date4j:

in your code, use date4j's DateTime to model date-time information.
in your database, use columns having data types which do not attempt to manage time zones for you.
if implicit time zones are sufficient for your users, consider not using your database at all for any time zone storage, or related calculations.
if implicit time zones are not sufficient for your users, then roll your own solution, and store them in a column of their own, separate from the date-time. (To be normalized, such a solution would usually require construction of a simple time zone table, to store some or all of the time zone identifiers known to Java - 'America/Montreal', 'Asia/Jakarta', and so on.)
Examples

Here are some quick examples of using date4j's DateTime class (more examples are available here):
DateTime dateAndTime = new DateTime("2010-01-19 23:59:59");
DateTime dateAndTime = new DateTime("2010-01-19T23:59:59.123456789");
DateTime dateOnly = new DateTime("2010-01-19");
DateTime timeOnly = new DateTime("23:59:59");
DateTime dateOnly = DateTime.forDateOnly(2010,01,19);
DateTime timeOnly = DateTime.forTimeOnly(23,59,59,0);

DateTime dt = new DateTime("2010-01-15 13:59:15");
boolean leap = dt.isLeapYear(); //false
dt.getNumDaysInMonth(); //31
dt.getStartOfMonth(); //2010-01-01, 00:00:00.000000000
dt.getEndOfDay(); //2010-01-15, 23:59:59.999999999
dt.format("YYYY-MM-DD"); //formats as '2010-01-15'
dt.plusDays(30); //30 days after Jan 15
dt.numDaysFrom(someDate); //returns an int
dueDate.lt(someDate); //less-than
dueDate.lteq(someDate); //less-than-or-equal-to
Although DateTime carries no TimeZone information internally, there are methods that take a TimeZone as a parameter:

DateTime now = DateTime.now(someTimeZone);
DateTime today = DateTime.today(someTimeZone);
DateTime fromMilliseconds = DateTime.forInstant(31313121L, someTimeZone);
birthday.isInFuture(someTimeZone);
dt.changeTimeZone(fromOneTimeZone, toAnotherTimeZone);
Explicit Time Zones

Many protest that it shouldn't be necessary to pass the time zone explicitly to the now and today methods. Naturally enough, they have become used to the JDK's style, after many years of use:
Date now = new Date();
Date4j's style of requiring the time zone is not an oversight, but a deliberate decision. Here are its justifications:
firstly, both the current date-time and the current date always depend on time zone. If you disagree, you're mistaken.
default time zones can lead to errors. If an application always runs in a single time zone, then there's usually no problem. But what if the application evolves? What if a client and server no longer run in the same time zone? Then disagreements can occur about what time it is - unless, for example, they agree explicitly on which time zone to use.
default time zones are a hidden dependency; an explicit time zone acts as a (slightly nagging) reminder that the time zone can be important in some contexts.
there's no compelling reason to follow the style of an API of such low quality as the JDK's date classes.
In summary, the date4j library takes the position that passing a time zone explicitly to its now and today methods helps it accomplish the main goal of a well-designed API: to increase the clarity of the code, and decrease the likelihood of programmer error.
Contributors

The contributors to the date4j project are:
John O'Hanley (Canada)
Piero Campalani (Italy)
Jean-Christophe Garnier (CERN - Switzerland)
Jamie Craane (Netherlands)
As of 2015-07-07, date4j resides in a github repository.
For interacting with JSON data, Giampaolo Trapasso (Italy) wrote a small adaptor to make DateTime compatible with the GSON library.

Maven, Gradle, Buildr Etc

For users of tools that can source the Maven Central Repository, Ian Darwin has kindly provided the following:
date4j artifacts in the Maven Central Repository
for building with Maven, a mavenized version of the source code, with a POM file
