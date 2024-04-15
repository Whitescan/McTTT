package dev.whitescan.mcttt.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {

    INNOCENT("§aInnocent"), TRAITOR("§4Traitor"), DETECTIVE("§9Detective");

    private final String displayName;

}
