package oth.shipeditor.parsing.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import oth.shipeditor.representation.VariantFile;
import oth.shipeditor.utility.text.StringConstants;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 22.10.2023
 */
public class VariantFileSerializer extends StdSerializer<VariantFile> {

    public VariantFileSerializer() {
        super(VariantFile.class);
    }

    protected VariantFileSerializer(Class<VariantFile> t) {
        super(t);
    }

    @Override
    public void serialize(VariantFile value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        gen.writeStringField(StringConstants.DISPLAY_NAME, value.getDisplayName());
        gen.writeNumberField(StringConstants.FLUX_CAPACITORS, value.getFluxCapacitors());
        gen.writeNumberField(StringConstants.FLUX_VENTS, value.getFluxVents());
        gen.writeBooleanField(StringConstants.GOAL_VARIANT, value.isGoalVariant());
        gen.writeStringField(StringConstants.HULL_ID, value.getShipHullId());

        provider.defaultSerializeField(StringConstants.HULL_MODS, value.getHullMods(), gen);

        List<String> permaMods = value.getPermaMods();
        if (permaMods != null && !permaMods.isEmpty()) {
            provider.defaultSerializeField(StringConstants.PERMA_MODS, permaMods, gen);
        }

        List<String> sMods = value.getSMods();
        if (sMods != null && !sMods.isEmpty()) {
            provider.defaultSerializeField(StringConstants.S_MODS, sMods, gen);
        }

        double quality = value.getQuality();
        if (quality >= 0) {
            gen.writeNumberField(StringConstants.QUALITY, quality);
        }
        gen.writeStringField(StringConstants.VARIANT_ID, value.getVariantId());

        provider.defaultSerializeField(StringConstants.WEAPON_GROUPS, value.getWeaponGroups(), gen);

        Map<String, String> modules = value.getModules();
        if (modules != null && !modules.isEmpty()) {
            provider.defaultSerializeField(StringConstants.MODULES, modules, gen);
        }

        List<String> wings = value.getWings();
        if (wings != null && !wings.isEmpty()) {
            provider.defaultSerializeField(StringConstants.WINGS, wings, gen);
        }

        gen.writeEndObject();
    }

}
