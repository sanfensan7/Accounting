package com.example.accounting.services.nlp

/**
 * 消费类别分类器
 * 根据消费描述和商户名称预测消费类别
 */
object CategoryClassifier {

    // 餐饮类关键词
    private val foodKeywords = listOf(
        "餐厅", "美食", "饭店", "小吃", "火锅", "烧烤", "快餐", "外卖",
        "食堂", "早餐", "午餐", "晚餐", "食品", "零食", "小吃", "水果",
        "甜点", "烘焙", "咖啡", "茶", "饮料", "酒水"
    )

    // 购物类关键词
    private val shoppingKeywords = listOf(
        "商场", "超市", "百货", "购物", "专卖店", "电商", "网购", "淘宝",
        "京东", "拼多多", "服装", "鞋帽", "箱包", "化妆品", "手机", "电器",
        "数码", "家具", "文具", "图书", "药店"
    )

    // 交通类关键词
    private val transportKeywords = listOf(
        "公交", "地铁", "出租车", "打车", "滴滴", "高铁", "火车", "飞机",
        "机票", "汽车", "加油", "停车", "高速", "过路费", "共享单车"
    )

    // 娱乐类关键词
    private val entertainmentKeywords = listOf(
        "电影", "游戏", "KTV", "酒吧", "演唱会", "音乐", "剧场", "门票",
        "景点", "旅游", "健身", "运动", "游泳", "球类", "玩具"
    )

    // 医疗类关键词
    private val medicalKeywords = listOf(
        "医院", "诊所", "医疗", "药店", "药物", "保健", "体检", "牙科",
        "眼科", "理疗", "中医", "西医", "门诊", "住院", "手术"
    )

    // 住房类关键词
    private val housingKeywords = listOf(
        "房租", "水电", "燃气", "物业", "宽带", "装修", "家居", "家电",
        "家具", "日用品", "清洁", "维修", "搬家", "酒店", "住宿"
    )

    // 商户名称到类别的映射缓存
    private val merchantCategoryCache = mutableMapOf<String, String>()

    /**
     * 根据商户名称预测消费类别
     */
    fun predictCategory(merchant: String): String {
        // 检查缓存中是否已有该商户的分类
        merchantCategoryCache[merchant]?.let {
            return it
        }

        // 根据关键词匹配预测类别
        val predictedCategory = when {
            containsAnyKeyword(merchant, foodKeywords) -> "餐饮"
            containsAnyKeyword(merchant, shoppingKeywords) -> "购物"
            containsAnyKeyword(merchant, transportKeywords) -> "交通"
            containsAnyKeyword(merchant, entertainmentKeywords) -> "娱乐"
            containsAnyKeyword(merchant, medicalKeywords) -> "医疗"
            containsAnyKeyword(merchant, housingKeywords) -> "住房"
            else -> "其他" // 默认类别
        }

        // 缓存结果
        merchantCategoryCache[merchant] = predictedCategory
        return predictedCategory
    }

    /**
     * 检查文本是否包含任何关键词
     */
    private fun containsAnyKeyword(text: String, keywords: List<String>): Boolean {
        return keywords.any { text.contains(it) }
    }

    /**
     * 添加或更新商户类别映射
     * 用于用户手动修正后的学习
     */
    fun updateMerchantCategory(merchant: String, category: String) {
        merchantCategoryCache[merchant] = category
    }
} 