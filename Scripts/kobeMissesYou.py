import sys
from PyQt5.QtWidgets import *
from PyQt5.QtGui import *
from PyQt5.QtCore import *
import requests
import json
import datetime

class MyWidget(QWidget):
    def __init__(self):
        super().__init__()

        # 创建托盘图标
        self.tray_icon = QSystemTrayIcon(self)
        self.tray_icon.setIcon(QIcon("res/miss.png"))
        self.tray_icon.setVisible(True)
        self.role=1 #1是小埋，2是坤坤
        # 创建右键菜单
        self.tray_menu = QMenu()
        if self.role==1:
            ident=QAction("我是：小埋", self)
        else:
            ident=QAction("我是：坤坤", self)
        self.tray_menu.addAction(ident)
        self.status_menu = QMenu("状态")
        self.tray_menu.addMenu(self.status_menu)
        self.status_menu.addAction("娱乐")
        self.status_menu.addAction("学习")
        self.status_menu.addAction("想你")
        self.upload_action = QAction("照片上传", self)
        self.tray_menu.addAction(self.upload_action)
        self.view_photo_menu = QMenu("查看照片")
        self.view_photo_menu.addAction("小埋")
        self.view_photo_menu.addAction("坤坤")
        self.tray_menu.addMenu(self.view_photo_menu)
        self.view_photo_menu.triggered.connect(self.on_view_photo_triggered)
        self.delete_action = QAction("删除照片", self)
        self.tray_menu.addAction(self.delete_action)
        self.tray_menu.addAction("退出")
        self.tray_menu.addSeparator()
        self.tray_icon.setContextMenu(self.tray_menu)
        self.screen_width = QDesktopWidget().screenGeometry().width()
        self.status1=[]
        self.status2=[]

        # 菜单事件
        self.status_menu.triggered.connect(self.on_status_triggered)
        self.tray_menu.triggered.connect(self.on_tray_triggered)
        self.upload_action.triggered.connect(self.on_upload_triggered)
        self.delete_action.triggered.connect(self.on_delete_triggered)

    def showEvent(self, event):
        self.move(self.screen_width - self.frameGeometry().width(), 10)

    def on_status_triggered(self, action):
        data={"id":self.role,
              "status":action.text(),
              "time":datetime.datetime.now().hour*60+datetime.datetime.now().minute}
        new_status=json.loads(requests.post('http://127.0.0.1:7002/setStatus',data=data,proxies={}).text)
        if self.role==1:
            self.status1=new_status
        else:
            self.status2=new_status
    def on_upload_triggered(self):
        file_path, _ = QFileDialog.getOpenFileName(self, "选择图片", "", "图片文件 (*.png *.jpg *.bmp)")
        if file_path:
            # 上传文件
            self.upload_file(file_path)
    def upload_file(self, file_path):
        with open(file_path, "rb") as f:
            files = {"file": f}
            data = {"id":self.role}
            try:
                if(requests.post("http://127.0.0.1:7002/uploadPhoto", data=data, files=files, proxies={}).text=="Success"):
                    QMessageBox.warning(self, "恭喜", "上传成功！")
                else:
                    QMessageBox.warning(self, "悲催了", "删除失败！")
            except:
                QMessageBox.warning(self, "悲催了", "删除失败！")

    def on_delete_triggered(self):
        data={"id":self.role}
        try:
            if(requests.post("http://127.0.0.1:7002/deletePhoto", data=data, proxies={}).text=="Success"):
                QMessageBox.warning(self, "恭喜", "删除成功！")
            else:
                QMessageBox.warning(self, "悲催了", "删除失败！")
        except:
            QMessageBox.warning(self, "悲催了", "删除失败！")

    def on_view_photo_triggered(self, action):
        id = 2 if action.text() == "坤坤" else 1
        response = requests.get('http://127.0.0.1:7002/getPhoto', params={"id": id}, proxies={})
        if response.text == "fail":
            QMessageBox.warning(self, "提示", "对方未上传照片！")
        else:
            # Create a QPixmap from the image data and display it in a new window
            pixmap = QPixmap()
            pixmap.loadFromData(response.content)
            # Scale the image if it exceeds the size limit
            if pixmap.width() > 800 or pixmap.height() > 800:
                pixmap = pixmap.scaled(800, 800, Qt.KeepAspectRatio)
            label = QLabel()
            label.setPixmap(pixmap)
            # Create the window and set its size and visibility
            window = QDialog()
            window.setWindowTitle(action.text()+"的照片")
            window.setFixedSize(pixmap.width(), pixmap.height())
            # Add the label to the window and show the window
            layout = QVBoxLayout()
            layout.addWidget(label)
            window.setLayout(layout)
            window.exec_()

    def on_tray_triggered(self, action):
        if action.text() == "退出":
            QApplication.quit()
            sys.exit(0)

    def mousePressEvent(self, event):
        if event.button() == Qt.LeftButton:
            self.drag_position = event.globalPos() - self.pos()
            event.accept()

    def mouseMoveEvent(self, event):
        if event.buttons() == Qt.LeftButton:
            self.move(event.globalPos() - self.drag_position)
            event.accept()

    def paintEvent(self, event):
        painter = QPainter(self)
        current_minute=datetime.datetime.now().hour*60+datetime.datetime.now().minute
        pixmap=QPixmap()
        if self.status1[-1]["status"]=="想你":
            pixmap.load("res/miss.png")
        else:
            pixmap.load("res/role1.png")
        pixmap = pixmap.scaled(60, 60)
        painter.drawPixmap(0,int(current_minute/2),pixmap)
        pixmap=QPixmap()
        if self.status2[-1]["status"]=="想你":
            pixmap.load("res/miss.png")
        else:
            pixmap.load("res/role2.png")
        pixmap = pixmap.scaled(60, 60)
        painter.drawPixmap(130,int(current_minute/2),pixmap)

        pen = QPen(Qt.black, 3)
        painter.setPen(pen)

        brush = QBrush(Qt.white)
        painter.setBrush(brush)
        rect = QRect(60, 30, 30, 720)
        painter.drawRect(rect)
        rect = QRect(100, 30, 30, 720)
        painter.drawRect(rect)
        pen = QPen(Qt.transparent, 3)
        painter.setPen(pen)
        for i in range(1,len(self.status1)):
            if self.status1[i-1]["status"]=="想你":
                brush = QBrush(QColor(255, 192, 203))
                painter.setBrush(brush)
            elif self.status1[i-1]["status"]=="娱乐":
                brush = QBrush(QColor(255, 165, 0))
                painter.setBrush(brush)
            elif self.status1[i-1]["status"]=="学习":
                brush = QBrush(Qt.blue)
                painter.setBrush(brush)
            rect = QRect(60, 30+int(self.status1[i-1]["time"]/2), 30, int(self.status1[i]["time"]/2-self.status1[i-1]["time"]/2))
            painter.drawRect(rect)
        if self.status1[-1]["status"]=="想你":
            brush = QBrush(QColor(255, 192, 203))
            painter.setBrush(brush)
        elif self.status1[-1]["status"]=="娱乐":
            brush = QBrush(QColor(255, 165, 0))
            painter.setBrush(brush)
        elif self.status1[-1]["status"]=="学习":
            brush = QBrush(Qt.blue)
            painter.setBrush(brush)
        rect = QRect(60, 30+int(self.status1[-1]["time"]/2), 30, int(current_minute/2-self.status1[-1]["time"]/2))
        painter.drawRect(rect)
        for i in range(1,len(self.status2)):
            if self.status2[i-1]["status"]=="想你":
                brush = QBrush(QColor(255, 192, 203))
                painter.setBrush(brush)
            elif self.status2[i-1]["status"]=="娱乐":
                brush = QBrush(QColor(255, 165, 0))
                painter.setBrush(brush)
            elif self.status2[i-1]["status"]=="学习":
                brush = QBrush(Qt.blue)
                painter.setBrush(brush)
            rect = QRect(100, 30+int(self.status2[i-1]["time"]/2), 30, int(self.status2[i]["time"]/2-self.status2[i-1]["time"]/2))
            painter.drawRect(rect)
        if self.status2[-1]["status"]=="想你":
            brush = QBrush(QColor(255, 192, 203))
            painter.setBrush(brush)
        elif self.status2[-1]["status"]=="娱乐":
            brush = QBrush(QColor(255, 165, 0))
            painter.setBrush(brush)
        elif self.status2[-1]["status"]=="学习":
            brush = QBrush(Qt.blue)
            painter.setBrush(brush)
        rect = QRect(100, 30+int(self.status2[-1]["time"]/2), 30, int(current_minute/2-self.status2[-1]["time"]/2))
        painter.drawRect(rect)
                
while True:
    try:
        status1=json.loads(requests.get('http://127.0.0.1:7002/getStatus1',proxies={}).text)
        status2=json.loads(requests.get('http://127.0.0.1:7002/getStatus2',proxies={}).text)
        break
    except:pass
app = QApplication(sys.argv)
app.setWindowIcon(QIcon())
window = MyWidget()
window.resize(200,800)
window.setWindowFlags(Qt.FramelessWindowHint)
window.setAttribute(Qt.WA_TranslucentBackground)
window.status1=status1
window.status2=status2
window.show()
sys.exit(app.exec_())
