	
package com.bfd.parse.entity;


public class Constants {
	public static final int JSONPARSER_FAILED = 4;
	public static final int STATUS_PLUGIN_READY = 2;
	
	/**
	 * 公共字段
	 */
	public static final String TMPL_ID = "tmpl_id";	//模板ID
	public static final String ITEMS = "items";	//新闻，商品，帖子
	public static final String ITEMLINK = "itemlink";//新闻，商品，帖子
	public static final String RAWLINK = "rawlink";	//新闻列表页
	public static final String LINKTYPE = "linktype";	//新闻列表页
	public static final String TASKS = "tasks";	// 调度任务队列
	public static final String COMMENT_URL = "comment_url"; //评论页
	public static final String NEXTPAGE = "nextpage"; //下一页
	public static final String VIEW_CNT = "view_cnt"; //浏览数
	public static final String VIEWS = "views"; //查看数：特指鼠标点击查看数
	public static final String VISIT_CNT = "visit_cnt"; //访问数   备注：算上刷新次数
	public static final String TODAY_VISIT_CNT = "today_visit_cnt"; //当日访问数   备注：算上刷新次数
	public static final String REPLY_CNT= "reply_cnt";//回复总数（电商，新闻）
	public static final String PARTAKE_CNT = "partake_cnt";//参与人数(论坛和新闻)
	public static final String RECOMMEND_CNT = "recommend_cnt";//推荐数(新闻内容)
	
	public static final String COMMENTER_IMG="commenter_img";//评论人的头像url(电商、新闻评论)
	public static final String COMMENTER_LEVEL = "commenter_level";//评论人等级(电商、新闻评论)
	public static final String POSTTIME = "posttime";//发表时间
	public static final String COMMENTS = "comments"; // 评论
	public static final String CATE = "cate";//品类
	public static final String COMMENT_TIME = "comment_time";//评论时间
	public static final String COMMENT_CONTENT = "comment_content";//评论内容
	public static final String USERNAME = "username"; // 用户名
	public static final String MSG_TYPE = "msg_type";//用来区分评论页和咨询页     评论页：comment 咨询页：consult
	public static final String COLLECTION_CNT = "collection_cnt"; //论坛、商品收藏数
	public static final String COMMENT_REPLY_CNT = "comment_reply_cnt";//评论的回复数（电商，新闻）
	public static final String CONTENTIMGS = "contentimgs";//内容中的图片
	
	public static final String POST_CNT = "post_cnt"; //发帖数(论坛帖子页)  帖子数(帖子列表页)
	public static final String USER_CNT = "user_cnt";//用户数(帖子列表页)
	public static final String TAG = "tag"; // 标签（新闻内容页，论坛帖子页）
	
	/**
	 * 论坛版块列表页
	 */
	public static final String BOARDNAME = "boardname";//版块名称
	public static final String BOARDLINK = "boardlink";//论坛链接
	
	/**
	 * 论坛帖子列表页
	 */
	public static final String PAGEIDX = "pageidx";  //页面ID
	public static final String LAST_REPLY_TIME = "last_reply_time";  //最后回复时间
	
	/**
	 * 帖子页
	 */
	//主贴信息
	public static final String CONTENTS = "contents"; //主贴内容
	public static final String NEWSTIME = "newstime"; //发表时间
	public static final String REPLYCOUNT= "replycount";//回复数
	
	//作者回复人资料链接
	public static final String AUTHORLINK = "authorlink"; //作者资料链接
	public static final String AUTHORNAME = "authorname"; //作者资料链接
	public static final String AUTHOR_LEVEL = "author_level";//作者等级
	public static final String REPLYLINK = "replylink"; //回复人资料链接
	
	//回复相关字段Constants
	public static final String REPLYUSERNAME = "replyusername"; //回复人名
	public static final String REPLYCONTENT = "replycontent"; //回复内容
	public static final String REPLYFLOOR = "replyfloor"; //回复楼层
	public static final String REPLYDATE = "replydate"; //回复时间
	public static final String REPLY_LEVEL = "reply_level"; //回复等级
	public static final String REPLYS = "replys"; //帖子回复 并列元素
	public static final String ESSENCE_CNT = "essence_cnt";//精华帖子数
	public static final String REPLY_ESSENCE_CNT = "reply_essence_cnt";//精华帖子数
	public static final String REPLY_POST_CNT = "reply_post_cnt";//回帖数
	public static final String REPLY_USER_CITY= "reply_user_city";//回复者所在城市
	public static final String REPLY_FORUM_MONEY= "reply_forum_money";//回复者论坛币
	public static final String REPLY_FORUM_SCORE= "reply_forum_score";//回复者积分
	public static final String REPLY_REG_TIME= "reply_reg_time";//回复者注册时间
	public static final String CHECKIN_DAYS= "checkin_days";//签到天数
	public static final String REPLY_CHECKIN_DAYS= "reply_checkin_days";//回复者签到天数
	public static final String REPLY_AVATAR = "reply_avatar"; //回复者头像链接
	public static final String TOPICCNT = "topiccnt"; //主题数
	public static final String CONCERN_CNT = "concern_cnt"; //关注数
	public static final String REPLY_FANS_CNT = "reply_fans_cnt"; //回复者粉丝数
	public static final String NEW_REPLY_TIME = "new_reply_time"; //最新回复时间
	public static final String REPLY_FROM = "reply_from"; //回复来自
	public static final String LAST_EDITOR = "last_editor"; //最后编辑人
	public static final String LAST_EDIT_TIME = "last_edit_time"; //最后编辑时间
	public static final String POPULARITY = "popularity"; //人气
	public static final String PRESTIGE = "prestige"; //威望
	
	/**
	 *  论坛用户资料页
	 */	
	public static final String MIID = "miid"; //用户唯一标识
	public static final String USERID = "userId"; //用户标识
	public static final String USERL_EVEL = "user_Level";  //用户等级
	public static final String USER_GROUP = "user_group"; //用户组
	public static final String PERSONAL_SIGNATURE = "personal_signature"; //个性签名
	public static final String EMAIL = "email"; //邮箱
	public static final String USER_CITY = "user_city"; //用户城市
	public static final String EXPERIENCE_CNT = "experience_cnt"; //经验值
	public static final String CONTRIBUTE_CNT = "contribute_cnt"; //贡献值
	public static final String FORUM_SCORE = "forum_score"; //论坛积分
	public static final String FORUM_MONEY = "forum_money"; // 论坛金币
	public static final String ACHIEVEMENT = "achievement"; // 成就
	
	public static final String SHARE_CNT = "share_cnt"; //分享数
	public static final String GOODFRIEND_NUM = "goodFriend_num"; //好友数量
	public static final String PAGELINK = "pagelink"; //主页链接
	public static final String REG_TIME = "reg_time"; //注册时间
	public static final String ONLINE_HOUR = "online_hour"; //在线时间
	public static final String LASTLOGIN_TIME = "lastLogin_time"; //最晚登录时间
	public static final String USER_BIRTHDAY = "user_birthday"; //生日
	public static final String NICKNAME = "nickname"; //呢称
	public static final String USER_NAME = "userName"; //用户名称
	public static final String LOGIN_CNT = "login_cnt"; //登录次数
	public static final String USERNAME_INFO = "userName"; //用户名
	public static final String FANS_CNT = "fans_cnt"; //粉丝数
	public static final String QUESTION_CNT = "question_cnt"; //提问数
	public static final String ANSWER_CNT = "answer_cnt"; //回答数
//	public static final String UP_CNT = "up_cnt"; //发言被顶数     //被顶数包括 FAVOR_CNT UP_CNT 两个字段本类型采用UP_CNT由于其他页面类型已存在此字段 故特意注释提醒
	
	/**
	 * 新闻列表页
	 */
	public static final String TITLE = "title"; // 标题
	public static final String LINK = "link"; // 链接
	public static final String NEWS_CNT = "news_cnt"; // 新闻总数
	
	/**
	 * 新闻内容页
	 */
	public static final String BUY_COUNT = "buy_cnt"; // 购买数 IT168  
	public static final String VIEW_COUNT = "view_count"; // 浏览数/访问数
	public static final String SOURCE = "source"; // 来源
	public static final String AUTHOR = "author"; // 作者
	public static final String EDITOR = "editor"; // 编辑
	public static final String CONTENT = "content"; // 内容
	public static final String KEYWORD = "keyword"; // 关键字
	public static final String COMPLAINTS_LIST = "complaints_list"; // 投诉榜-质量万里行
	public static final String POST_TIME = "post_time"; // 发表时间
	public static final String ARTICLE_RANKING = "article_ranking"; // 文章排名
	public static final String NEW_EGGNUM = "eggNum"; //新闻扔鸡蛋的人数
	public static final String NEW_FLOWERNUM = "flowerNum"; // 新闻送鲜花的人数
	public static final String PLAY_CNT = "play_cnt"; // 播放次数
	public static final String MSG_CNT = "msg_cnt"; // 留言数
	public static final String ARTICLE_CNT = "article_cnt"; // 作者发表的文章数
	public static final String JREPLY_CNT = "jreply_cnt"; // 参与评论的总人数
	public static final String GOLDPEN_CNT = "goldPen_cnt"; // 拥有金笔数
	public static final String SENDGOLDPEN_CNT = "sendGoldPen_cnt"; // 送出金笔数
	public static final String SUPPORT_CNT = "support_cnt"; // 新闻内容点赞数
	public static final String OPPOSE_CNT = "oppose_cnt"; // 新闻内容点赞数
	
	/**
	 * 新闻评论页
	 */
	public static final String CITY = "city"; // 评论人所在城市
	public static final String UP_CNT = "up_cnt"; // 评论顶的人数
	public static final String DOWN_CNT = "down_cnt"; // 评论踩的人数
	public static final String COMMENTER_IP = "commenter_ip"; //回复人ip
	public static final String MOBILE = "mobile";//评论人手机类型
	public static final String COM_REPLY_CNT = "com_reply_cnt";//评论的回复数
	public static final String REFER_COMMENTS = "refer_comments";//引用的回复
	public static final String REFER_COMM_USERNAME = "refer_comm_username";// 引用的回复的用户名
	public static final String REFER_COMM_CONTENT = "refer_comm_content";//引用的回复的内容
	public static final String REFER_REPLYFLOOR = "refer_replyfloor";//引用的回复的回复人楼层
	public static final String REFER_COMM_TIME = "refer_comm_time";//引用的回复的时间
	public static final String REFER_COMM_CITY = "refer_comm_city";//引用的回复的回复人城市
	public static final String REFER_UP_CNT = "refer_up_cnt";//引用的回复的支持数(推荐数)
	public static final String REFER_DOWN_CNT = "refer_down_cnt";//引用的回复的反对数
	
	/**
	 * 电商列表页
	 */
	public static final String ITEMNAME = "itemname"; //商品名称
	public static final String TOTAL_CNT = "total_cnt";//电商列表总商品个数，用于判断翻页
	public static final String QUANTITY = "quantity";//商品销量
	public static final String STORENAME = "storename";//店铺名称
	
	/**
	 * 电商详情页
	 */
	public static final String PRICE = "price"; //商品价格
	public static final String STOREUPPERPRICE="storeupperprice"; // 商城最高价
	public static final String STORELOWERPRICE="storelowerprice"; // 商城最低价
	public static final String MARKETPRICE="marketprice"; // 市场价
	public static final String MARKETUPPERPRICE="marketupperprice"; // 市场最高价
	public static final String MARKETLOWERPRICE="marketlowerprice"; // 市场最低价
	public static final String LARGE_IMG = "large_img"; //大图
	public static final String SMALL_IMG = "small_img"; //小图
	public static final String PROMOTIONINFO = "promotioninfo"; //促销信息
	public static final String BRIEF = "brief"; //简介
	public static final String ITEM_NUM = "item_num"; //商品编码
	public static final String BRAND_NAME = "brand_name"; //品牌名称
	public static final String HOT_SALE = "hot_sale"; //热销商品
	public static final String CONSULT_URL="consult_url";   //电商咨询url
	public static final String PARAMETER = "parameter";     //电商参数
	public static final String PROPS_EVALUATION = "props_evaluation";     //商品属性评分
	public static final String BUYER_IMPRESSION = "buyer_impression";   //买家印象
	public static final String AVERAGE = "average";   //商品平均分
	public static final String GUID = "guid";   
	
	/**
	 * 电商咨询页
	 *
	 */
	public static final String CONSULTATIONS ="consultations";
	public static final String CONSULT_TIME="consult_time";       //咨询时间
	public static final String CONSULT_CONTENT="consult_content"; //咨询内容
	public static final String CONSULTER_NAME="consulter_name";//咨询人
	//public static final String CONSULT_USERNAME="consult_username";//咨询人 
	public static final String CONSULT_REPLYS="consult_replys";  //咨询回复
	
	/**
	 * 电商评论页
	 */
	public static final String COMMENTER_NAME = "commenter_name"; //评论人
	public static final String FAVOR_CNT = "favor_cnt";//点赞数
	public static final String GOOD_CNT = "good_cnt"; //好评数
	public static final String GENERAL_CNT = "general_cnt"; //中评数
	public static final String POOR_CNT = "poor_cnt"; //差评数
	public static final String WITHPIC_CNT = "withpic_cnt";//有图片的评价数量
	public static final String AGAIN_CNT = "again_cnt";//追加评论数量
	public static final String BEST_CNT = "best_cnt";//精华评论
	public static final String GOOD_RATE = "good_rate"; //好评率
	public static final String GENERAL_RATE = "general_rate"; //中评率
	public static final String POOR_RATE = "poor_rate"; //差评率
	public static final String COMMENT_IMG = "comment_img"; //评价晒图
	public static final String COMMENT_TAG = "comment_tag"; //评价标签
	public static final String SCORE = "score"; //打分
	//public static final String CONSULTATION = "consultation"; //咨询内容
	public static final String BUY_TIME = "buy_time"; //购买时间
	public static final String BUY_TYPE = "buy_type"; //购买机型
	public static final String COLOR = "color"; //购买机身颜色
	public static final String BUY_RAM = "buy_ram"; //购买机身内存
	public static final String VERSION = "version"; //版本
	public static final String LOCATION = "location"; //所在地
	public static final String CUSTOMER_ID="custId";  //顾客id
//	public static final String GRADE_CODE="gradeCode"; //评级（星号）
	public static final String COMMENT_REPLY="comment_reply"; //评论回复
	public static final String COMMENT_REPLY_NAME="comment_reply_name"; //对评论进行的回复  用户名
	public static final String COMMENT_REPLY_TIME="comment_reply_time"; //对评论进行的回复  发表时间
	public static final String COMMENT_REPLY_CONTENT="comment_reply_content"; //对评论进行回复  回复内容
	public static final String COMMENTER_REG_TIME = "commenter_reg_time";//对评论进行回复  回复人的注册时间（中关村产品）
	public static final String COMMENT_LEVEL="comment_level"; //好评，中评，差评
	public static final String COMMENT_KEYWORDS="comment_keywords"; //评论关键词
	public static final String COMMENT_ID="commentId"; //评论id
	public static final String SUPPLIERNAME="suppliername";  //供应商名字
	public static final String SOURCESYSTEM="sourceSystem";  //评论来自什么系统(如:pc,安卓）
	public static final String SHOPTNAME = "shopname";                  //卖家名称
	public static final String COMMENT_PROPS_EVALUATION = "comment_props_evaluation"; //评论中的属性评价
	public static final String BUYER_CREDIT = "buyer_credit"; //买家信用积分 拍拍荣耀旗舰店
	public static final String APPEND_COMMENT_TIME = "append_comment_time"; //追加评论的时间(淘宝，天猫这种追评)
	public static final String APPEND_COMMENT_CONTENT = "append_comment_content"; //追加评论的内容(淘宝，天猫这种追评)
	public static final String APPEND_COMMENT_REPLY = "append_comment_reply"; //追加评论的回复(淘宝，天猫这种追评)
	public static final String APPEND_COMMENT_IMG="append_comment_img"; //对评论进行回复  回复图片（string集合）
	
	public static final String COMMENT_TITLE="comment_title";  //评论标题 (Emobile163)
	
	/**
	 * 解析结果代码
	 */
	public static final int SUCCESS = 0;
	public static final int NOFOUND_TEMPLATE = 500001;
	public static final int NOFOUND_PREPROCESSOR = 500002;
	public static final int NOFOUND_REPROCESSOR = 500003;
	public static final int NOFOUND_JSONPROCESSOR = 500004;
	public static final int NEEDLOGIN = 500005;
	public static final int NOHTMLDATA = 500006;
	
	public static final int GETIID_FAILED = 500008;
	public static final int FAILED = 500009;
	public static final int PREPROCESSOR_FAIL = 500010;
	public static final int REPROCESS_FAILED = 500011;
	public static final int JSONPROCESS_FAILED = 500012;
	public static final int OFF = 500016;
	public static final int PARSECODE_DWONLOADFAILED = 500018;
	public static final int COOKIE_NOUSE = 500019;
	public static final int UNCOMPRESS_FAIL = 50020;
	public static final int NOFOUND_SITEPAGECONFIG = 500021;
	public static final int JSONPARSEERROR = 500022;
	public static final int WEIBOPARSE_ERROR = 500023;
	
	public static final int REPROCESS_NONESAVE = -3;// 不保存，直接返回，以后不再调用
	public static final int NOT_ITEM_TASK = -4;
	public static final int AUTO_PARSE_FAILED = -5;
	/**
	 * 视频列表页
	 */
}