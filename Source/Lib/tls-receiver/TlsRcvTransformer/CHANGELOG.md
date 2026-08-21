# CHANGELOG TlsRcvTransformer / receiver-transformer

## 2.0.0

- Set new version / no change to content

## 1.3.0

No change here.

## 1.2.5

Enable adding Typ48 interval data controlled by config.

## 1.2.4

No change here

## 1.2.3

- No change here (.gitignore)

## 1.2.2

- No change here

## 1.2.1

- Changed logging to org.apache.logging.log4j

## 1.2.0

- de.heuboe.base:jhbBase 3.0.7 -> 3.1.0
- org.junit.jupiter:junit-jupiter-engine 5.7.0 -> 5.8.2

## 1.1.25

- No change here

## 1.1.24

- Reduced the number of messages. No more complaints on missing timestamps when jobnr is not zero (i.e. requested data).
- Removed log warnings concerning unexpected timestamps de when communication with GPRS-UZ

## 1.1.23

- No change here

## 1.1.22

- No change here

## 1.1.21

- Introduced counter for errors (exceptions) during getter calls. Implies spring boot and micrometer.

## 1.1.20

- Removed analysis code

## 1.1.19

- Added heuristic for analysis of subsequent delivered data

## 1.1.18

- Inherit: !!! Revert version of tls-tele from 0.5.1 to 0.4. in order to avoid later reuquired error prone version pinning

## 1.1.17

- No change here

## 1.1.16

- Removed special debug code

## 1.1.15

- Added code for handling of DE-Blocks from GPRS-UZ

## 1.1.14

- Added support for SystemMessageManagement

## 1.1.13

- No change here

## 1.1.12

- Added 'jobnummer' to all data objects

## 1.1.11

- Fixed bug of wrong lve timestamps

## 1.1.10

- Fixed the use of the timezone provided by applications properties. Hopefully the last time.

## 1.1.9

- No change here

## 1.1.8

- No change here

## 1.1.7

- Added pom scm info

## 1.1.4

* Changed resolution of list items: made it recursive
