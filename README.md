# 📱 课程表 App — Android

**课余实践项目**

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.10.01-brightgreen)
![Room](https://img.shields.io/badge/Room-2.6.1-orange)
![ML Kit](https://img.shields.io/badge/ML%20Kit-16.0.1-blue)
![Android](https://img.shields.io/badge/Android-API%2026%2B-lightgreen)

> 基于 **Jetpack Compose** + **Room** + **ML Kit** 的课程表管理 Android App。
> 支持手动添加课程、OCR 拍照识别导入课程表、学期管理、周次切换、课程冲突检测等功能。

---

## ✨ 功能特性

### 📅 课程管理
- 手动添加/编辑/删除课程
- 课程信息：名称、教师、教室、上课周次、节次
- 课程冲突检测（同一时间/地点冲突自动提示）

### 🗓 学期与周次
- 学期起止日期设置
- 自动计算当前周次
- 支持切换查看不同周次的课程

### 📸 OCR 导入课程表（ML Kit）
- 调用摄像头拍照
- 基于 **Google ML Kit 中文文字识别** 自动识别课程表
- 解析识别结果，批量导入课程数据
- 支持从相册选择图片

### 🎨 界面
- Material 3 设计风格
- 周视图课程网格展示
- 深色/浅色主题切换
- 课程详情弹出面板

---

## 💻 技术栈

| 技术 | 用途 |
|------|------|
| **Kotlin** | 开发语言 |
| **Jetpack Compose** | 声明式 UI |
| **Material 3** | UI 设计组件 |
| **Room** | 本地数据库 |
| **Navigation Compose** | 页面导航 |
| **ViewModel + StateFlow** | MVVM 架构 |
| **CameraX** | 摄像头控制 |
| **ML Kit Text Recognition** | OCR 中文文字识别 |
| **Coil** | 图片加载 |

---

## 🏗 项目架构

```
com.example.coursetable/
├── CourseTableApplication.kt     # Application 入口
├── MainActivity.kt               # 主 Activity + 导航
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt        # Room 数据库
│   │   ├── CourseDao.kt          # 课程数据访问
│   │   └── ScheduleDao.kt        # 周次安排访问
│   └── model/
│       ├── Course.kt             # 课程实体
│       └── Schedule.kt           # 周次安排实体
├── ocr/
│   └── OCRProcessor.kt           # ML Kit OCR 处理
├── ui/
│   ├── components/
│   │   ├── CourseCard.kt         # 课程卡片组件
│   │   ├── CourseDetailSheet.kt  # 课程详情弹窗
│   │   ├── CourseGrid.kt         # 课程网格视图
│   │   └── WeekSelector.kt       # 周次选择器
│   ├── screens/
│   │   ├── HomeScreen.kt         # 首页（课程表视图）
│   │   ├── AddCourseScreen.kt    # 添加课程页面
│   │   ├── OCRImportScreen.kt    # OCR 导入页面
│   │   └── SettingsScreen.kt     # 设置页面
│   └── theme/
│       ├── Color.kt              # 颜色定义
│       ├── Theme.kt              # 主题配置
│       └── Type.kt               # 字体排版
└── viewmodel/
    └── CourseViewModel.kt        # 课程业务逻辑
```

---

## 📸 截图

| 首页周视图 | 添加课程 | OCR 导入 |
|-----------|---------|---------|
| _(运行截图位置)_ | _(运行截图位置)_ | _(运行截图位置)_ |

---

## 🚀 快速开始

### 前置要求

- Android Studio (推荐最新版本)
- JDK 17+
- Android SDK 36+
- 真机或模拟器（相机功能需要真机）

### 步骤

1. **克隆项目**

```bash
git clone https://github.com/0127dy/course-table-app.git
```

2. **用 Android Studio 打开项目**

选择 `File → Open`，选择项目根目录 `course-table-app`

3. **等待 Gradle 同步完成**

4. **运行到模拟器或真机**

> ⚠️ OCR 导入功能需要 **真机摄像头**，模拟器不支持

---

## 📦 构建 APK

```bash
./gradlew assembleDebug
```

APK 生成在：`app/build/outputs/apk/debug/app-debug.apk`

---

## 📋 TODO / 后续计划

- [ ] 导出课程表为图片/日历格式
- [ ] 自定义课程颜色标签
- [ ] 节次时段自定义
- [ ] 考试安排管理

---

## 📧 联系

项目作者：端木 · [GitHub](https://github.com/0127dy)
