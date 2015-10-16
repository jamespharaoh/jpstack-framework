package wbs.clients.apn.chat.user.core.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import wbs.clients.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.sms.number.core.model.NumberRec;

public
interface ChatUserDaoMethods {

	ChatUserRec find (
			ChatRec chat,
			NumberRec number);

	int countOnline (
			ChatRec chat,
			ChatUserType type);

	List<ChatUserRec> findOnline (
			ChatRec chat);

	List<ChatUserRec> findOnline (
			ChatUserType type);

	List<ChatUserRec> findWantingBill (
			Date date);

	List<ChatUserRec> findWantingWarning ();

	List<ChatUserRec> findAdultExpiryLimit (
			int maxResults);

	List<ChatUserRec> find (
			ChatRec chat,
			ChatUserType type,
			Orient orient,
			Gender gender);

	List<ChatUserRec> findWantingJoinOutbound ();

	List<ChatUserRec> findWantingAdultAd ();

	List<Integer> searchIds (
			Map<String,Object> searchMap);

	List<ChatUserRec> find (
			ChatAffiliateRec chatAffiliate);

	List<ChatUserRec> findDating (
			ChatRec chat);

	List<ChatUserRec> findWantingAd ();

	List<ChatUserRec> findWantingQuietOutbound ();

}