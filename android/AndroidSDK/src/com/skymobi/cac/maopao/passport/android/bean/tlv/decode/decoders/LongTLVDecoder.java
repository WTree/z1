/**
 * 
 */
package com.skymobi.cac.maopao.passport.android.bean.tlv.decode.decoders;

import com.skymobi.cac.maopao.passport.android.bean.tlv.decode.TLVDecodeContext;
import com.skymobi.cac.maopao.passport.android.bean.tlv.decode.TLVDecoder;


/**
 * @author hp
 *
 */
public class LongTLVDecoder implements TLVDecoder {

    //@SuppressWarnings("unused")
	//private static final Logger logger = LoggerFactory.getLogger(LongTLVDecoder.class);
    
	/* (non-Javadoc)
	 * @see com.skymobi.bean.tlv.TLVDecoder#decode(int, byte[], com.skymobi.bean.tlv.TLVDecodeContext)
	 */
	public Object decode(int tlvLength, byte[] tlvValue, TLVDecodeContext ctx) {
		return	ctx.getNumberCodec().bytes2Long(tlvValue, tlvLength);
	}

}
