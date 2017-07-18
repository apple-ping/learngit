package cn.com.flaginfo.flow.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.com.flaginfo.flow.constants.Constants;
import cn.com.flaginfo.flow.pojo.Card;
import cn.com.flaginfo.flow.service.impl.CardService;
import cn.com.flaginfo.flow.service.impl.RecordService;
import cn.com.flaginfo.flow.utils.JSONUtils;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/card")
public class CardController extends BaseController {
	@Autowired
	private CardService cardService;

	@Autowired
	private RecordService recordService;

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getPayCard", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> getPayCard(@RequestBody JSONObject param) {

		if (isEmptyInMap(param, "spid")) {
			return getDataErrorView("spid不能空.");
		}
		Card card = new Card();
		card.setSpId(String.valueOf(param.optString("spid")));
		List<Card> cards = cardService.getPayCard(card);
		return getListView(cards);
	}

	/**
	 * 生成流量卡
	 * 
	 * @author limin
	 * @param card
	 * @param cardCount
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> create(@RequestBody JSONObject param) {
		try {
			cardService.createCard(param);
		} catch (Exception e) {
			e.printStackTrace();
			return getExeFailView("异常! error:" + e.getMessage());
		}
		return getSuccView();
	}

	@RequestMapping(value = "/query", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> query(@RequestBody JSONObject param) {
		Map<String, Object> retMap = getSuccView();
		try {

			Card card = (Card) JSONUtils.toPageBean(param, Card.class);
			List<Card> resultList = cardService.query(card);
			retMap.put(Constants.FLD_DATAS,filterResult(JSONUtils.fromObjects(resultList), null) );
			retMap.put(Constants.FLD_PAGE, card.getPage());

		} catch (Exception e) {
			e.printStackTrace();
			getExeFailView("异常! error:" + e.getMessage());
		}
		return retMap;
	}

	@ResponseBody
	@RequestMapping(value = "/exportList", method = RequestMethod.POST)
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Object> exportList(@RequestBody JSONObject param) {
		Map<String, Object> retMap = getSuccView();
		try {

			Card card = new Card();
			if (!isEmptyInMap(param, "type")) { // 判断运营商是否为空
				card.setType((Integer.valueOf(param.get("type").toString())));
			}
			if (!isEmptyInMap(param, "status")) { // 判断流量卡状态是否为空
				card.setStatus((Integer.valueOf(param.get("status").toString())));
			}
			if (!isEmptyInMap(param, "startTime")) { // 判断起始时间是否为空
				card.setStartTime(param.optString("startTime"));
			}
			if (!isEmptyInMap(param, "endTime")) { // 判断结束时间是否为空
				card.setEndTime(param.optString("startTime"));
			}
			card.setSpId(param.optString("spId"));

			List<Card> resultList = cardService.exportList(card);

			retMap.put(Constants.FLD_DATAS, JSONUtils.fromObjects(resultList));
		} catch (Exception e) {
			return getExeFailView("异常! error:" + e.getMessage());
		}
		return retMap;
	}

	@RequestMapping(value = "/exchangeFlowCard", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> exchangeFlowCard(@RequestBody JSONObject param) {
		Map<String, Object> retMap = getSuccView();
		try {

			// 卡密码
			String password = param.optString("password");
			// 手机号
			String phoneNo = param.optString("phone");
			Card card = new Card();
			card.setPassword(password);
			String flag = cardService.exchangeFlowCard(card, phoneNo, password);
			if (flag != null) {
				retMap = getNullErrorView(flag);
			} else {
				retMap = getSuccView();
			}
		} catch (Exception e) {
			retMap = getExeFailView("异常! error:" + e.getMessage());
		}
		return retMap;
	}

	@RequestMapping(value = "/sendNotice", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> sendNotice(@RequestBody JSONObject param) {
		Map<String, Object> retMap = getSuccView();

		String result = cardService.sendNotice(param);
		if (result != null) {
			retMap = getExeFailView(result);
		}
		return retMap;
	}

}
