/**
 * 
 */
package com.skymobi.android.bean.tlv.decode.decoders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skymobi.android.bean.tlv.annotation.TLVAttribute;
import com.skymobi.android.bean.tlv.decode.TLVDecodeContext;
import com.skymobi.android.bean.tlv.decode.TLVDecodeContextFactory;
import com.skymobi.android.bean.tlv.decode.TLVDecoder;
import com.skymobi.android.bean.tlv.decode.TLVDecoderOfBean;
import com.skymobi.android.bean.tlv.meta.TLVFieldMetainfo;
import com.skymobi.android.bean.util.meta.Int2TypeMetainfo;


/**
 * @author hp
 *
 */
public class ShortTLVDecoder implements TLVDecoder {

    @SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ShortTLVDecoder.class);
    
	/* (non-Javadoc)
	 * @see com.skymobi.bean.tlv.TLVDecoder#decode(int, byte[], com.skymobi.bean.tlv.TLVDecodeContext)
	 */
	public Object decode(int tlvLength, byte[] tlvValue, TLVDecodeContext ctx) {
		return	ctx.getNumberCodec().bytes2Short(tlvValue, tlvLength);
	}

}
