# tls-tele-recorder

Logs all telegrams into files and makes them available via a TCP/IP interface. Telegrams from different directions 
(receive and send) are written into the same file.

## Configuration
* `tls.tele.recorder.receiveTopic`: The Kafka topic the received telegrams will be read from.
* `tls.tele.recorder.sendTopic`: The Kafka topic the sent telegrams will be read from.
* `tls.tele.recorder.absolutTelegramPath`: The absolute path where telegrams should be saved at as file.
* `tls.tele.recorder.cleanTelegrams`: The amount of telegrams that should be saved. The amount of telegrams will cycle. 
    The value `0` will lead to an unlimited amount of telegrams.
* `tls.tele.recorder.server.path.readTelegrams`: Defines the endpoint for reading all telegrams that are saved as file 
    on the server. Default is `read`.
* `tls.tele.recorder.server.path.streamTelegrams`: Defines the endpoint for streaming all incoming telegrams. Default
    is `stream`.
* `server.port`: Controls the port of the server. Default is `8080`.


## Server endpoints
The following server endpoints are available to receive telegrams. The last path element is configurable via properties.
* `http://<server>:<port>/teleRecorder/read`: Receive all telegrams that are currently stored on the server. The count 
    depends on property the `cleanTelegrams` (see Configuration). Depending on the property `fileFormat` the 
    telegram will be sent as JSON or in a binary format. 
* `http://<server>:<port>/teleRecorder/stream`: Continuous streaming of telegrams in binary format.

Both endpoints send multiple telegrams. At the end of one telegram two line breaks (`\r\n`) will be sent.
