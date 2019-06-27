# LOG ANALYTICS


## Running

Run this using [sbt](http://www.scala-sbt.org/).  If you downloaded this project from <https://github.com/amitkmrjha/loganalytics>

```bash
sbt clean
sbt compile
sbt run
```


## Test the Rest API

upload a log file to  http://localhost:9000/upload/log

## Test through browser

 go to <http://localhost:9000/log> choose the log file to upload


## Controllers

- `LogController.scala`:

  Shows how to handle file upload requests.


## Swagger pec 3.0

/public/swagger/openapi.json