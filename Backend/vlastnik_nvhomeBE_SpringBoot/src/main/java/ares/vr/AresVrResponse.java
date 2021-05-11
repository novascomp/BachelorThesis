package ares.vr;

import ares.vr.AresVrEndpoint;
import ares.vr.json.AresSingle;
import ares.vr.json.Odpoved;
import ares.vr.json.VypisVR;
import ares.vr.basic.AresBasic;
import ares.vr.json.AresResponseChecker;
import ares.vr.multiple.AresMultiple;
import ares.vr.multiple.OdpovedMultiple;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.novascomp.home.config.BeansInit;
import me.novascomp.utils.standalone.service.exceptions.InternalException;

public class AresVrResponse {

    private final String json;
    private final ObjectMapper objectMapper;
    private String organizationName;

    private static final Logger LOG = Logger.getLogger(AresVrResponse.class.getName());
    private final List<VypisVR> vypisVRs;

    public AresVrResponse(String json, ObjectMapper objectMapper) {
        this.json = json;
        this.objectMapper = objectMapper;
        this.vypisVRs = new ArrayList<>();
        analyzeRecordsCount(getRecordsCount());
    }

    private Integer getRecordsCount() {
        try {
            AresBasic aresBasic = objectMapper.readValue(json, AresBasic.class);
            return Integer.valueOf(aresBasic.odpoved.pocet_zaznamu);
        } catch (JsonProcessingException ex) {
            LOG.log(Level.INFO, ex.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            Logger.getLogger(AresVrResponse.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void analyzeRecordsCount(Integer recordsCount) {

        try {
            if (recordsCount == 1) {
                AresSingle aresSingle = objectMapper.readValue(json, AresSingle.class);
                if (Optional.ofNullable(aresSingle.odpoved).isPresent()) {
                    vypisVRs.add(aresSingle.odpoved.vypis_VR);
                }
            }
            if (recordsCount >= 2) {
                AresMultiple aresMultiple = objectMapper.readValue(json, AresMultiple.class);
                if (Optional.ofNullable(aresMultiple.odpoved).isPresent()) {
                    aresMultiple.odpoved.vypis_VR.forEach((vypisVR) -> {
                        vypisVRs.add(vypisVR);
                    });
                }
            }

        } catch (JsonProcessingException ex) {
            LOG.log(Level.INFO, json);
            LOG.log(Level.INFO, ex.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);

            try {
                if (recordsCount == 1) {
                    AresSingle aresSingle = objectMapper.readValue(AresVrEndpoint.fixClenArray(json), AresSingle.class);
                    if (Optional.ofNullable(aresSingle.odpoved).isPresent()) {
                        vypisVRs.add(aresSingle.odpoved.vypis_VR);
                    }
                }
                if (recordsCount >= 2) {
                    AresMultiple aresMultiple = objectMapper.readValue(AresVrEndpoint.fixClenArray(json), AresMultiple.class);
                    if (Optional.ofNullable(aresMultiple.odpoved).isPresent()) {
                        aresMultiple.odpoved.vypis_VR.forEach((vypisVR) -> {
                            vypisVRs.add(vypisVR);
                        });
                    }
                }
            } catch (JsonProcessingException ex2) {
                LOG.log(Level.INFO, json);
                LOG.log(Level.INFO, ex2.toString());
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                throw new InternalException("");
            }
        }

        if (AresResponseChecker.checkZakladniUdajeObchodniFirma(vypisVRs.get(0))) {
            organizationName = vypisVRs.get(0).zakladniUdaje.obchodniFirma.value;
        }
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public List<VypisVR> getVypisVRs() {
        return vypisVRs;
    }

}
