/**
 * 
 */
package com.skymobi.cac.maopao.passport.android.bean.tlv.decode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hp
 *
 */
public class DefaultDecoderRepository implements TLVDecoderRepository {

	private Map<Class<?>, TLVDecoder> decoders = new HashMap<Class<?>, TLVDecoder>();

	public void add(Class<?> cls, TLVDecoder decoder) {
		decoders.put(cls, decoder);
	}
	
    /**
     * @param encoders the encoders to set
     */
    public void setDecoders(Map<Class<?>, TLVDecoder> decoders) {
        this.decoders.clear();
        
        for (Map.Entry<Class<?>, TLVDecoder> entry : decoders.entrySet()) {
            if ( null != entry.getValue() ) {
                this.decoders.put(entry.getKey(), entry.getValue());
            }
        }
    }
    
	public Map<Class<?>, TLVDecoder> getDecoders() {
		return Collections.unmodifiableMap( decoders );
	}
	
	/* (non-Javadoc)
	 * @see com.skymobi.bean.tlv.TLVDecoderRepository#getDecoderOf(java.lang.Class)
	 */
	public TLVDecoder getDecoderOf(Class<?> cls) {
		do {
			TLVDecoder decoder = decoders.get(cls);
	        if ( null != decoder ) {
	        	return	decoder;
	        }
	        if ( Object.class.equals(cls) ) {
	        	break;
	        }
        	cls = cls.getSuperclass();
		} while ( cls != null );
		
		return	null;
	}

    public Map<String, String> getDecodersAsText() {
    	Map<String, String>	ret = new HashMap<String, String>();
        for (Map.Entry<Class<?>, TLVDecoder> entry : this.decoders.entrySet()) {
        	ret.put(entry.getKey().toString(), entry.getValue().toString());
        }
        
        return	ret;
    }
}
