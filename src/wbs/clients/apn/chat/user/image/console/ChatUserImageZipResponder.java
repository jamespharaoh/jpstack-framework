package wbs.clients.apn.chat.user.image.console;

import java.io.IOException;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.ConsoleResponder;

@PrototypeComponent ("chatUserImageZipResponder")
public
class ChatUserImageZipResponder
	extends ConsoleResponder {

	@Inject
	ConsoleRequestContext requestContext;

	List<ChatUserRec> chatUsers;

	@Override
	@SuppressWarnings ("unchecked")
	public
	void prepare () {

		chatUsers =
			(List<ChatUserRec>)
			requestContext.request ("chatUsers");

	}

	@Override
	public
	void goHeaders () {

		requestContext.setHeader (
			"Content-Type",
			"application/zip");

		requestContext.setHeader (
			"Content-Disposition",
			"download; filename=photos.zip");

	}

	@Override
	public
	void goContent ()
		throws IOException {

		ZipOutputStream zipOutputStream =
			new ZipOutputStream (
				requestContext.outputStream ());

		zipOutputStream.setMethod (
			ZipOutputStream.STORED);

		for (ChatUserRec chatUser
				: chatUsers) {

			ZipEntry zent =
				new ZipEntry (chatUser.getCode () + ".jpg");

			ChatUserImageRec chatUserImage =
				chatUser.getChatUserImageList ().get (0);

			byte[] data =
				chatUserImage.getMedia ().getContent ().getData ();

			zent.setSize (data.length);
			zent.setCrc (getCrc32 (data));
			zent.setTime (chatUserImage.getTimestamp ().getTime ());

			zipOutputStream.putNextEntry (zent);
			zipOutputStream.write (data);
			zipOutputStream.closeEntry ();

		}

		zipOutputStream.finish ();
		zipOutputStream.close ();

	}

	private static
	long getCrc32 (
			byte[] data) {

		CRC32 crc32 =
			new CRC32 ();

		crc32.update (
			data);

		return crc32.getValue ();

	}

}