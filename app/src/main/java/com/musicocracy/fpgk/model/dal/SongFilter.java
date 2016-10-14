package com.musicocracy.fpgk.model.dal;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "SongFilter")
public class SongFilter {
    SongFilter() {} // OrmLite requires a default constructor

    public SongFilter(MusicService service, String songId, Party party, FilterMode filterMode) {
        this.service = service;
        this.songId = songId;
        this.party = party;
        this.filterMode = filterMode;
    }

    public MusicService getService() {
        return service;
    }

    public String getSongId() {
        return songId;
    }

    public Party getParty() {
        return party;
    }

    public FilterMode getFilterMode() {
        return filterMode;
    }

    @DatabaseField private MusicService service;
    @DatabaseField private String songId;
    @DatabaseField(foreign = true) private Party party;
    @DatabaseField private FilterMode filterMode;
}