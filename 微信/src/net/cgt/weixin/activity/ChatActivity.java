package net.cgt.weixin.activity;

import java.lang.reflect.Field;
import java.util.Date;

import net.cgt.weixin.R;
import net.cgt.weixin.domain.User;
import net.cgt.weixin.utils.AppToast;
import net.cgt.weixin.utils.HandlerTypeUtils;
import net.cgt.weixin.utils.L;
import net.cgt.weixin.utils.LogUtil;
import net.cgt.weixin.view.manager.XmppManager;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * 聊天
 * 
 * @author lijian
 * @date 2014-12-04
 */
public class ChatActivity extends BaseActivity implements OnClickListener {

	private static final String LOGTAG = LogUtil.makeLogTag(ChatActivity.class);

	protected static final int FLAG_RECEIVER = 100;

	private User user;
	private ScrollView mSv_chat_showBoxScrollView;
	private LinearLayout mLl_chat_showBox;
	private Button mBtn_chat_speech;
	private Button mBtn_chat_keyboard;
	private LinearLayout mLl_chat_inputBox;
	private Button mBtn_chat_smilingface;
	private Button mBtn_chat_pressToTalk;
	private Button mBtn_chat_plus;
	private EditText mEt_chat_input;
	private Button mBtn_chat_send;
	private Vibrator vibrator;

	/** 代码注册一个广播接收者(临时的) **/
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String from = intent.getStringExtra("from");
			String body = intent.getStringExtra("body");
			L.i(LOGTAG, "receiver--->from=" + from + "---body=" + body);
			Message message = handler.obtainMessage();
			message.what = FLAG_RECEIVER;
			Bundle bundle = new Bundle();
			bundle.putString("from", from);
			bundle.putString("body", body);
			message.setData(bundle);
			message.sendToTarget();
		}
	};

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HandlerTypeUtils.WX_HANDLER_TYPE_LOAD_DATA_SUCCESS:
				setChatDataMe();
				break;
			case HandlerTypeUtils.WX_HANDLER_TYPE_LOAD_DATA_FAIL:
				AppToast.getToast().show("消息发送失败");
				break;
			case FLAG_RECEIVER:// 收到广播
				setChatDataOtherParty(msg);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	/**
	 * 设置对方聊天数据
	 * 
	 * @param msg
	 */
	private void setChatDataOtherParty(Message msg) {
		Bundle bundle = msg.getData();
		String body = bundle.getString("body");

		View v = View.inflate(this, R.layout.cgt_layout_chat_dialogbox_otherparty, null);
		TextView mTv_chat_dialogBox = (TextView) v.findViewById(R.id.cgt_tv_chat_dialogBox_otherparty);
		ImageView mIv_chat_userImg = (ImageView) v.findViewById(R.id.cgt_iv_chat_userImg_otherparty);

		mIv_chat_userImg.setImageResource(Integer.parseInt(user.getUserPhote()));
		mTv_chat_dialogBox.setText(body);
		mLl_chat_showBox.addView(v);

		setChatWindowToDown();
	}

	/**
	 * 设置聊天窗口滚动到最底下
	 */
	private void setChatWindowToDown() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				mSv_chat_showBoxScrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

	/**
	 * 设置我的聊天数据
	 */
	private void setChatDataMe() {
		mEt_chat_input.setText("");

		View v = View.inflate(this, R.layout.cgt_layout_chat_dialogbox_me, null);
		TextView mTv_chat_dialogBox = (TextView) v.findViewById(R.id.cgt_tv_chat_dialogBox_me);
		ImageView mIv_chat_userImg = (ImageView) v.findViewById(R.id.cgt_iv_chat_userImg_me);

		mIv_chat_userImg.setImageResource(R.drawable.user_picture);
		mTv_chat_dialogBox.setText(msg);
		mLl_chat_showBox.addView(v);
		setChatWindowToDown();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cgt_activity_chat);
		Intent intent = this.getIntent();
		user = intent.getParcelableExtra("user");
		init();
	}
	
	private void init() {
		initView();
		initData();
	}

	private void initView() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(user.getUserAccount());
		setOverflowShowingAlways();

		mSv_chat_showBoxScrollView = (ScrollView) findViewById(R.id.cgt_sv_chat_showBoxScrollView);
		mLl_chat_showBox = (LinearLayout) findViewById(R.id.cgt_ll_chat_showBox);
		mBtn_chat_speech = (Button) findViewById(R.id.cgt_btn_chat_speech);
		mBtn_chat_keyboard = (Button) findViewById(R.id.cgt_btn_chat_keyboard);
		mLl_chat_inputBox = (LinearLayout) findViewById(R.id.cgt_ll_chat_input_box);
		mEt_chat_input = (EditText) findViewById(R.id.cgt_et_chat_input);
		mBtn_chat_smilingface = (Button) findViewById(R.id.cgt_btn_chat_smilingface);
		mBtn_chat_pressToTalk = (Button) findViewById(R.id.cgt_btn_chat_pressToTalk);
		mBtn_chat_plus = (Button) findViewById(R.id.cgt_btn_chat_plus);
		mBtn_chat_send = (Button) findViewById(R.id.cgt_btn_chat_send);
		mBtn_chat_send.setOnClickListener(this);
	}

	private void initData() {
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		IntentFilter filter = new IntentFilter("net.cgt.weixin.chat");
		registerReceiver(receiver, filter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cgt_btn_chat_send://发送按钮
			if (checkValidity()) {
				sendMsg();
			}
			break;

		default:
			break;
		}
	}

	private String msg = "";

	/**
	 * 检查合法性
	 * 
	 * @return
	 */
	private boolean checkValidity() {
		msg = mEt_chat_input.getText().toString().trim();
		if (TextUtils.isEmpty(msg)) {
			mEt_chat_input.requestFocus();
			AppToast.getToast().show("发送消息不能为空");
			Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
			mEt_chat_input.startAnimation(shake);
			vibrator.vibrate(300);
			return false;
		}
		return true;
	}

	/**
	 * 发送消息
	 */
	private void sendMsg() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Chat chat = XmppManager.getInstance().getFriendChat(user.getUserAccount(), null);

				//	friend为好友名，不是JID；listener 监听器可以传null，利用聊天窗口对象调用sendMessage发送消息
				//	这里sendMessage我传的是一个JSON字符串，以便更灵活的控制，发送消息完成~
				try {
					// String msgjson = "{\"messageType\":\""+messageType+"\",\"chanId\":\""+chanId+"\",\"chanName\":\""+chanName+"\"}";
					// chat.sendMessage(msgjson);

					chat.sendMessage(msg);
					handler.sendEmptyMessage(HandlerTypeUtils.WX_HANDLER_TYPE_LOAD_DATA_SUCCESS);
				} catch (XMPPException e) {
					handler.sendEmptyMessage(HandlerTypeUtils.WX_HANDLER_TYPE_LOAD_DATA_FAIL);
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.cgt_menu_chat, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home://返回上一菜单页
			AppToast.getToast().show("返回上一页");
			Intent upIntent = NavUtils.getParentActivityIntent(this);
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
			} else {
				upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				NavUtils.navigateUpTo(this, upIntent);
			}
			break;
		case R.id.menu_chat_chatInfo:
			AppToast.getToast().show(R.string.text_menu_chatInfo);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 通过反射得到Android的有无物理Menu键<br>
	 * setOverflowShowingAlways()方法则是屏蔽掉物理Menu键，不然在有物理Menu键的手机上，overflow按钮会显示不出来
	 */
	private void setOverflowShowingAlways() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			menuKeyField.setAccessible(true);
			menuKeyField.setBoolean(config, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
