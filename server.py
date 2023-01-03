from flask import *
import datetime
import json
from werkzeug.utils import secure_filename
import os
from flask_apscheduler import APScheduler

app = Flask(__name__)

status1 = [{"status": "想你", "time": 0}]
status2 = [{"status": "想你", "time": 0}]
with open('status1.json', 'w') as f:
    json.dump(status1, f)
with open('status2.json', 'w') as f:
    json.dump(status2, f)

@app.route("/uploadPhoto", methods=["POST"])
def upload_photo():
    id = request.form.get("id")
    file = request.files.get("file")
    if file and id:
        # Create the directory for storing the photos if it doesn't exist
        if not os.path.exists("photos"):
            os.makedirs("photos")
        # Sanitize the file name and construct the file path
        filename = secure_filename(file.filename)
        # Get the file extension
        _, file_extension = os.path.splitext(filename)
        # Construct the file path
        filepath = os.path.join("photos", id + file_extension)
        # Delete any existing files with the same name (ignoring the file extension)
        for f in os.listdir("photos"):
            if f.startswith(id):
                os.remove(os.path.join("photos", f))
        # Save the file to the file path
        file.save(filepath)
        return "Success"
    return "Error"

@app.route("/deletePhoto", methods=["POST"])
def delete_photo():
    id = request.form.get("id")
    for f in os.listdir("photos"):
        if f.startswith(id):
            filepath = os.path.join("photos", f)
            os.remove(filepath)
    return "Success"

@app.route("/getPhoto")
def get_photo():
    id = request.args.get("id")
    for f in os.listdir("photos"):
        if f.startswith(id):
            filepath = os.path.join("photos", f)
            mimetype = 'image/' + f.rsplit('.', 1)[1]
            return send_file(filepath, mimetype=mimetype)
    return "fail"


@app.route("/getStatus1")
def get_status1():
    with open('status1.json', 'r') as f:
        status1=json.load(f)
    return json.dumps(status1,ensure_ascii=False)

@app.route("/getStatus2")
def get_status2():
    with open('status2.json', 'r') as f:
        status2=json.load(f)
    return json.dumps(status2,ensure_ascii=False)

@app.route("/setStatus", methods=["POST"])
def set_status():
    id = request.form.get("id")
    status = request.form.get("status")
    time = request.form.get("time")

    if id == "1":
        status1.append({"status": status, "time": int(time)})
        with open('status1.json', 'w') as f:
            json.dump(status1, f)
        return json.dumps(status1,ensure_ascii=False)
    elif id == "2":
        status2.append({"status": status, "time": int(time)})
        with open('status2.json', 'w') as f:
            json.dump(status2, f)
        return json.dumps(status2,ensure_ascii=False)
    else:
        return "Invalid id"

class Config(object):
    JOBS = [
        {
            'id': 'job1',
            'func': 'server:reset_statuses',
            'trigger': 'cron',
            'day': '*',
            'hour': '0',
            'minute': '0',
            'second': '0'
        }
    ]
    SCHEDULER_API_ENABLED = True
    
def reset_statuses():
    status1 = [{"status": "想你", "time": 0}]
    status2 = [{"status": "想你", "time": 0}]
    with open('status1.json', 'w') as f:
        json.dump(status1, f)
    with open('status2.json', 'w') as f:
        json.dump(status2, f)


if __name__ == "__main__":
    app.config.from_object(Config())
    scheduler = APScheduler()
    scheduler.init_app(app)
    scheduler.start()
    app.run(host="0.0.0.0",port=7002)
