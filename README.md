# 智能记账应用

一款专注于高效记账的移动应用，提供多种记账方式和自动记账功能。

## 核心功能

- **智能记账**：支持语音记账、文字智能记账和截图记账
- **支付自动检测**：监测微信和支付宝支付，自动弹出记账窗口
- **数据统计**：提供多维度的收支分析和可视化
- **资产管理**：多账户管理、借贷记录和预算控制

## 技术栈

- Android
- Kotlin
- Room数据库
- MVVM架构
- Accessibility Service (无障碍服务)
- ML Kit (OCR和自然语言处理)

## 项目结构

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/accounting/
│   │   │   ├── MainActivity.kt
│   │   │   ├── components/     # UI组件
│   │   │   ├── screens/        # 应用页面
│   │   │   ├── services/       # 业务逻辑
│   │   │   │   ├── accessibility/  # 无障碍服务
│   │   │   │   ├── nlp/            # 自然语言处理
│   │   │   │   └── ocr/            # 图像识别
│   │   │   ├── models/         # 数据模型
│   │   │   ├── repositories/   # 数据仓库
│   │   │   └── utils/          # 工具类
│   │   └── res/                # 资源文件
│   └── androidTest/            # 测试代码
├── build.gradle
└── proguard-rules.pro
```

## 安装与使用

1. 克隆项目到本地
2. 使用Android Studio打开项目
3. 编译并安装到设备上

## 权限说明

应用需要以下权限：
- 麦克风权限（语音记账）
- 存储权限（截图记账）
- 无障碍服务权限（自动记账）

## 贡献指南

欢迎提交Issue和Pull Request改进项目。 