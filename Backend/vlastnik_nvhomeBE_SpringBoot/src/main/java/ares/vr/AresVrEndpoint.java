package ares.vr;

import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.novascomp.home.config.BeansInit;
import me.novascomp.utils.standalone.service.exceptions.InternalException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;

public class AresVrEndpoint {

    private static final Logger LOG = Logger.getLogger(AresVrEndpoint.class.getName());

    public static String getAresVerejnyRejstrikByIcoRawResponse(String ico) throws ServiceException {
        try {
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            LOG.log(Level.INFO, "USING EXTERNAL RESOURCE");
            LOG.log(Level.INFO, "Connecting to ARES");
            SAXReader reader = new SAXReader();
            org.dom4j.Document document = reader.read("https://wwwinfo.mfcr.cz/cgi-bin/ares/darv_vr.cgi?ico=" + ico);
            //org.dom4j.Document document = reader.read("ico.xml");
            LOG.log(Level.INFO, "DONE ARES Connected");
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return xml2Json(document.asXML()).toJSONString();
        } catch (DocumentException ex) {
            LOG.log(Level.INFO, ex.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            throw new InternalException("");
        }
    }

    public static String fixClenArray(String json) {
        JSONObject root = JSON.parseObject(json);
        JSONObject statutarniOrgan = root.getJSONObject("Odpoved").getJSONObject("Vypis_VR").getJSONObject("StatutarniOrgan");
        JSONObject clen = (JSONObject) statutarniOrgan.remove("Clen");
        JSONArray array = new JSONArray();
        array.add(clen);
        statutarniOrgan.put("Clen", array);
        return root.toJSONString();
    }

    //using https://www.programmersought.com/article/44332615131/
    private static JSONObject xml2Json(String xmlStr) throws DocumentException {
        Document doc = DocumentHelper.parseText(xmlStr);
        JSONObject json = new JSONObject();
        dom4j2Json(doc.getRootElement(), json);
        return json;
    }

    //using https://www.programmersought.com/article/44332615131/
    private static void dom4j2Json(Element element, JSONObject json) {
        List<Element> chdEl = element.elements();
        chdEl.forEach(e -> {
            if (!e.elements().isEmpty()) {
                JSONObject chdjson = new JSONObject();
                dom4j2Json(e, chdjson);
                Object o = json.get(e.getName());
                if (o != null) {
                    JSONArray jsona = null;
                    if (o instanceof JSONObject) {
                        JSONObject jsono = (JSONObject) o;
                        json.remove(e.getName());
                        jsona = new JSONArray();
                        jsona.add(jsono);
                        jsona.add(chdjson);
                    }
                    if (o instanceof JSONArray) {
                        jsona = (JSONArray) o;
                        jsona.add(chdjson);
                    }
                    json.put(e.getName(), jsona);
                } else {
                    if (!chdjson.isEmpty()) {
                        json.put(e.getName(), chdjson);
                    }
                }
            } else {
                if (!e.getText().isEmpty()) {
                    json.put(e.getName(), e.getText());
                }
            }
        });
    }
}
