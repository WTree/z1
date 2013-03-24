/**
 * 
 */
package com.skymobi.cac.maopao.passport.android.bean.tlv.decode.decoders;

import java.util.Date;

import com.skymobi.cac.maopao.passport.android.bean.tlv.decode.TLVDecodeContext;
import com.skymobi.cac.maopao.passport.android.bean.tlv.decode.TLVDecoder;



/**
 * @author hp
 *
 */
public class DateTLVDecoder implements TLVDecoder {

    //@SuppressWarnings("unused")
	//private static final Logger logger = LoggerFactory.getLogger(DateTLVDecoder.class);
    
	/* (non-Javadoc)
	 * @see com.skymobi.bean.tlv.TLVDecoder#decode(int, byte[], com.skymobi.bean.tlv.TLVDecodeContext)
	 */
	public Object decode(int tlvLength, byte[] tlvValue, TLVDecodeContext ctx) {
		return	new Date(ctx.getNumberCodec().bytes2Long(tlvValue, tlvLength));
	}

}
