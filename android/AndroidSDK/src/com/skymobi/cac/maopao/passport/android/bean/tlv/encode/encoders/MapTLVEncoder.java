
package com.skymobi.cac.maopao.passport.android.bean.tlv.encode.encoders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.skymobi.cac.maopao.passport.android.bean.tlv.encode.TLVEncodeContext;
import com.skymobi.cac.maopao.passport.android.bean.tlv.encode.TLVEncoder;
import com.skymobi.cac.maopao.passport.android.util.ByteUtils;



public class MapTLVEncoder implements TLVEncoder {
    //private static final Logger logger = 
    //	LoggerFactory.getLogger(MapTLVEncoder.class);

    public List<byte[]> encode(Object src, TLVEncodeContext ctx) {
        List<byte[]> ret = new ArrayList<byte[]>();

		if ( src instanceof Map ) {
            Map map = (Map) src;
            if(map.isEmpty()){
                return Collections.emptyList();
            }
            for(Object entryKey : map.keySet()){
                Object entryValue = map.get(entryKey);
                
            	if ( null == entryKey || null == entryValue ) {
            	//	logger.warn("entry key or value is null.");
            		continue;
            	}
            	
                TLVEncoder keyEncoder = ctx.getEncoderRepository().getEncoderOf(entryKey.getClass());
                TLVEncoder valueEncoder = ctx.getEncoderRepository().getEncoderOf(entryValue.getClass());
                
                if ( null == keyEncoder || null == valueEncoder ) {
            		//logger.warn("can't found key/value's encoder");
            		continue;
                }
                
                List<byte[]> dest = keyEncoder.encode(entryKey,ctx);
                //	len
                ret.add( ctx.getNumberCodec().int2Bytes(ByteUtils.totalByteSizeOf(dest), 4) );
                ret.addAll(dest);

                dest = valueEncoder.encode(entryValue, ctx);
                //	len
                ret.add( ctx.getNumberCodec().int2Bytes(ByteUtils.totalByteSizeOf(dest), 4) );
                ret.addAll(dest);
            }

		}
		else {
			//logger.error("MapTLVEncoder: wrong source type. [{}]", src.getClass());
			throw new RuntimeException("MapTLVEncoder: wrong source type. [" + src.getClass() + "]");
		}

		return	ret;
    }
}
