# CHANGELOG receiver-reader

## 2.0.0

- Set new version / no change to content

## 1.3.0

No change here.

## 1.2.5

Reject duplicate items in DataObject. Assure no duplicate items when Adding Fg1 interval data. ()

## 1.2.4

Improved grammar (EOF at end)

## 1.2.3 '

- New approach (java 8 time api) to analyse time stamps (now TimeGetter)

## 1.2.3

- An intermediate fix in TimeGetter for a rare problem with timestamps at the
 beginning of a new longer month after a shorter month.
- Preparation for a new approach (java 8 time api) to analyse time stamps
 (NewTimeGetter) which will later replace the current approach.

## 1.2.2

- Added '#Puffer' to predefined variables as integer. #Puffer indicates subsequent delivered data.

## 1.2.1

- Changed logging to org.apache.logging.log4j

## 1.2.0

- de.heuboe.base:jhbBase-logging 3.0.7 -> 3.1.0
- org.junit.jupiter:junit-jupiter-engine 5.7.0 -> 5.8.2

## 1.1.25

- Corrected blockgetter. Access to size byte in byte block was too early. => recv 4/1/3

## 1.1.24

- No change here

## 1.1.23

- Corrected use of STRING with a little hack. STRINGs named 'textzeichen' are handled with ISO-8859-1 and other with ASCII.
  Affects StringGetter and StringItem.

## 1.1.22

- Off by 1 in BlockGetter corrected. This allows empty arrays of status.]
- Added getAsLong implementation [Long.valueOf( (long) ( Math.floor( value ) ) )] in FloatItem. This allows 5/3 = 1. 

## 1.1.21

- Quick fix for invalid values in UFD type float. InvalidFunction has to be improved.

## 1.1.20

- No change here

## 1.1.19

- No change here

## 1.1.18

- Inherit: !!! Revert version of tls-tele from 0.5.1 to 0.4. in order to avoid later reuquired error prone version pinning

## 1.1.17

- Allow a value to be set which indicates that floating point values are invalid (in class FunctionInval)

## 1.1.16

- No change here

## 1.1.15

- No change here

## 1.1.14

- Added Support for SystemMessageManagement

## 1.1.13

- No change here

## 1.1.12

- Changed invalid value -1 in float context to Float.MIN_VALUE.

## 1.1.11

- Changed dependencies

## 1.1.10

- No change here

## 1.1.9

- Alex added support for 'AS' (target type) to more getter classes and syntax

## 1.1.8

- hopefully the last change of ArrayGetter: In case of rule result item is list, add every list element

## 1.1.7

- Updated dependencies, especially antlr
- added pom scm info

## 1.1.6

* Fixed logging in TimeGetter.setTimeZone

## 1.1.5

Moved dependency from jupiter to scope test and version to 5.5.2 

## 1.1.4

* Change timezone handling in TimeGetter
  * Added property for timezone to operate on tls timestamps
  * Added static method to set timezone for process
  * timezone ID is used and has to be valid and exact: e.g. 'GMT+01:00' or 'Europe/Berlin' or UTC
  * the calendar for TLS time interpretation is initialised with this timezone
  * default is Europe/Berlin
* IfGetter/OptionalGetter
  * Removed warning
  * if a rule yields a ListItem, it is added as is
  * resolution of sublists is done late in the transformer
