package com.shslab.shstube.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeleteOrUpdateAdapter;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.coroutines.FlowUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import java.lang.Class;
import java.lang.Long;
import java.lang.NullPointerException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class DownloadDao_Impl implements DownloadDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<DownloadEntity> __insertAdapterOfDownloadEntity;

  private final EntityDeleteOrUpdateAdapter<DownloadEntity> __updateAdapterOfDownloadEntity;

  public DownloadDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfDownloadEntity = new EntityInsertAdapter<DownloadEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `downloads` (`id`,`url`,`title`,`mime`,`source`,`formatId`,`isAudioOnly`,`status`,`progress`,`speedBps`,`totalBytes`,`downloadedBytes`,`localPath`,`errorMsg`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final DownloadEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getUrl() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getUrl());
        }
        if (entity.getTitle() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getTitle());
        }
        if (entity.getMime() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getMime());
        }
        if (entity.getSource() == null) {
          statement.bindNull(5);
        } else {
          statement.bindText(5, entity.getSource());
        }
        if (entity.getFormatId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindText(6, entity.getFormatId());
        }
        final int _tmp = entity.isAudioOnly() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getStatus() == null) {
          statement.bindNull(8);
        } else {
          statement.bindText(8, entity.getStatus());
        }
        statement.bindLong(9, entity.getProgress());
        statement.bindLong(10, entity.getSpeedBps());
        statement.bindLong(11, entity.getTotalBytes());
        statement.bindLong(12, entity.getDownloadedBytes());
        if (entity.getLocalPath() == null) {
          statement.bindNull(13);
        } else {
          statement.bindText(13, entity.getLocalPath());
        }
        if (entity.getErrorMsg() == null) {
          statement.bindNull(14);
        } else {
          statement.bindText(14, entity.getErrorMsg());
        }
        statement.bindLong(15, entity.getCreatedAt());
        statement.bindLong(16, entity.getUpdatedAt());
      }
    };
    this.__updateAdapterOfDownloadEntity = new EntityDeleteOrUpdateAdapter<DownloadEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `downloads` SET `id` = ?,`url` = ?,`title` = ?,`mime` = ?,`source` = ?,`formatId` = ?,`isAudioOnly` = ?,`status` = ?,`progress` = ?,`speedBps` = ?,`totalBytes` = ?,`downloadedBytes` = ?,`localPath` = ?,`errorMsg` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final DownloadEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getUrl() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getUrl());
        }
        if (entity.getTitle() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getTitle());
        }
        if (entity.getMime() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getMime());
        }
        if (entity.getSource() == null) {
          statement.bindNull(5);
        } else {
          statement.bindText(5, entity.getSource());
        }
        if (entity.getFormatId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindText(6, entity.getFormatId());
        }
        final int _tmp = entity.isAudioOnly() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getStatus() == null) {
          statement.bindNull(8);
        } else {
          statement.bindText(8, entity.getStatus());
        }
        statement.bindLong(9, entity.getProgress());
        statement.bindLong(10, entity.getSpeedBps());
        statement.bindLong(11, entity.getTotalBytes());
        statement.bindLong(12, entity.getDownloadedBytes());
        if (entity.getLocalPath() == null) {
          statement.bindNull(13);
        } else {
          statement.bindText(13, entity.getLocalPath());
        }
        if (entity.getErrorMsg() == null) {
          statement.bindNull(14);
        } else {
          statement.bindText(14, entity.getErrorMsg());
        }
        statement.bindLong(15, entity.getCreatedAt());
        statement.bindLong(16, entity.getUpdatedAt());
        statement.bindLong(17, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final DownloadEntity item, final Continuation<? super Long> $completion) {
    if (item == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      return __insertAdapterOfDownloadEntity.insertAndReturnId(_connection, item);
    }, $completion);
  }

  @Override
  public Object update(final DownloadEntity item, final Continuation<? super Unit> $completion) {
    if (item == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __updateAdapterOfDownloadEntity.handle(_connection, item);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Object getById(final long id, final Continuation<? super DownloadEntity> $completion) {
    final String _sql = "SELECT * FROM downloads WHERE id = ? LIMIT 1";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfUrl = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "url");
        final int _columnIndexOfTitle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "title");
        final int _columnIndexOfMime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "mime");
        final int _columnIndexOfSource = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "source");
        final int _columnIndexOfFormatId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "formatId");
        final int _columnIndexOfIsAudioOnly = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "isAudioOnly");
        final int _columnIndexOfStatus = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "status");
        final int _columnIndexOfProgress = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "progress");
        final int _columnIndexOfSpeedBps = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "speedBps");
        final int _columnIndexOfTotalBytes = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "totalBytes");
        final int _columnIndexOfDownloadedBytes = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "downloadedBytes");
        final int _columnIndexOfLocalPath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "localPath");
        final int _columnIndexOfErrorMsg = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "errorMsg");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "createdAt");
        final int _columnIndexOfUpdatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "updatedAt");
        final DownloadEntity _result;
        if (_stmt.step()) {
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          final String _tmpUrl;
          if (_stmt.isNull(_columnIndexOfUrl)) {
            _tmpUrl = null;
          } else {
            _tmpUrl = _stmt.getText(_columnIndexOfUrl);
          }
          final String _tmpTitle;
          if (_stmt.isNull(_columnIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = _stmt.getText(_columnIndexOfTitle);
          }
          final String _tmpMime;
          if (_stmt.isNull(_columnIndexOfMime)) {
            _tmpMime = null;
          } else {
            _tmpMime = _stmt.getText(_columnIndexOfMime);
          }
          final String _tmpSource;
          if (_stmt.isNull(_columnIndexOfSource)) {
            _tmpSource = null;
          } else {
            _tmpSource = _stmt.getText(_columnIndexOfSource);
          }
          final String _tmpFormatId;
          if (_stmt.isNull(_columnIndexOfFormatId)) {
            _tmpFormatId = null;
          } else {
            _tmpFormatId = _stmt.getText(_columnIndexOfFormatId);
          }
          final boolean _tmpIsAudioOnly;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfIsAudioOnly));
          _tmpIsAudioOnly = _tmp != 0;
          final String _tmpStatus;
          if (_stmt.isNull(_columnIndexOfStatus)) {
            _tmpStatus = null;
          } else {
            _tmpStatus = _stmt.getText(_columnIndexOfStatus);
          }
          final int _tmpProgress;
          _tmpProgress = (int) (_stmt.getLong(_columnIndexOfProgress));
          final long _tmpSpeedBps;
          _tmpSpeedBps = _stmt.getLong(_columnIndexOfSpeedBps);
          final long _tmpTotalBytes;
          _tmpTotalBytes = _stmt.getLong(_columnIndexOfTotalBytes);
          final long _tmpDownloadedBytes;
          _tmpDownloadedBytes = _stmt.getLong(_columnIndexOfDownloadedBytes);
          final String _tmpLocalPath;
          if (_stmt.isNull(_columnIndexOfLocalPath)) {
            _tmpLocalPath = null;
          } else {
            _tmpLocalPath = _stmt.getText(_columnIndexOfLocalPath);
          }
          final String _tmpErrorMsg;
          if (_stmt.isNull(_columnIndexOfErrorMsg)) {
            _tmpErrorMsg = null;
          } else {
            _tmpErrorMsg = _stmt.getText(_columnIndexOfErrorMsg);
          }
          final long _tmpCreatedAt;
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt);
          final long _tmpUpdatedAt;
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt);
          _result = new DownloadEntity(_tmpId,_tmpUrl,_tmpTitle,_tmpMime,_tmpSource,_tmpFormatId,_tmpIsAudioOnly,_tmpStatus,_tmpProgress,_tmpSpeedBps,_tmpTotalBytes,_tmpDownloadedBytes,_tmpLocalPath,_tmpErrorMsg,_tmpCreatedAt,_tmpUpdatedAt);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public LiveData<List<DownloadEntity>> observeAll() {
    final String _sql = "SELECT * FROM downloads ORDER BY createdAt DESC";
    return __db.getInvalidationTracker().createLiveData(new String[] {"downloads"}, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfUrl = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "url");
        final int _columnIndexOfTitle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "title");
        final int _columnIndexOfMime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "mime");
        final int _columnIndexOfSource = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "source");
        final int _columnIndexOfFormatId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "formatId");
        final int _columnIndexOfIsAudioOnly = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "isAudioOnly");
        final int _columnIndexOfStatus = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "status");
        final int _columnIndexOfProgress = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "progress");
        final int _columnIndexOfSpeedBps = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "speedBps");
        final int _columnIndexOfTotalBytes = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "totalBytes");
        final int _columnIndexOfDownloadedBytes = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "downloadedBytes");
        final int _columnIndexOfLocalPath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "localPath");
        final int _columnIndexOfErrorMsg = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "errorMsg");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "createdAt");
        final int _columnIndexOfUpdatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "updatedAt");
        final List<DownloadEntity> _result = new ArrayList<DownloadEntity>();
        while (_stmt.step()) {
          final DownloadEntity _item;
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          final String _tmpUrl;
          if (_stmt.isNull(_columnIndexOfUrl)) {
            _tmpUrl = null;
          } else {
            _tmpUrl = _stmt.getText(_columnIndexOfUrl);
          }
          final String _tmpTitle;
          if (_stmt.isNull(_columnIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = _stmt.getText(_columnIndexOfTitle);
          }
          final String _tmpMime;
          if (_stmt.isNull(_columnIndexOfMime)) {
            _tmpMime = null;
          } else {
            _tmpMime = _stmt.getText(_columnIndexOfMime);
          }
          final String _tmpSource;
          if (_stmt.isNull(_columnIndexOfSource)) {
            _tmpSource = null;
          } else {
            _tmpSource = _stmt.getText(_columnIndexOfSource);
          }
          final String _tmpFormatId;
          if (_stmt.isNull(_columnIndexOfFormatId)) {
            _tmpFormatId = null;
          } else {
            _tmpFormatId = _stmt.getText(_columnIndexOfFormatId);
          }
          final boolean _tmpIsAudioOnly;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfIsAudioOnly));
          _tmpIsAudioOnly = _tmp != 0;
          final String _tmpStatus;
          if (_stmt.isNull(_columnIndexOfStatus)) {
            _tmpStatus = null;
          } else {
            _tmpStatus = _stmt.getText(_columnIndexOfStatus);
          }
          final int _tmpProgress;
          _tmpProgress = (int) (_stmt.getLong(_columnIndexOfProgress));
          final long _tmpSpeedBps;
          _tmpSpeedBps = _stmt.getLong(_columnIndexOfSpeedBps);
          final long _tmpTotalBytes;
          _tmpTotalBytes = _stmt.getLong(_columnIndexOfTotalBytes);
          final long _tmpDownloadedBytes;
          _tmpDownloadedBytes = _stmt.getLong(_columnIndexOfDownloadedBytes);
          final String _tmpLocalPath;
          if (_stmt.isNull(_columnIndexOfLocalPath)) {
            _tmpLocalPath = null;
          } else {
            _tmpLocalPath = _stmt.getText(_columnIndexOfLocalPath);
          }
          final String _tmpErrorMsg;
          if (_stmt.isNull(_columnIndexOfErrorMsg)) {
            _tmpErrorMsg = null;
          } else {
            _tmpErrorMsg = _stmt.getText(_columnIndexOfErrorMsg);
          }
          final long _tmpCreatedAt;
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt);
          final long _tmpUpdatedAt;
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt);
          _item = new DownloadEntity(_tmpId,_tmpUrl,_tmpTitle,_tmpMime,_tmpSource,_tmpFormatId,_tmpIsAudioOnly,_tmpStatus,_tmpProgress,_tmpSpeedBps,_tmpTotalBytes,_tmpDownloadedBytes,_tmpLocalPath,_tmpErrorMsg,_tmpCreatedAt,_tmpUpdatedAt);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Flow<List<DownloadEntity>> flowAll() {
    final String _sql = "SELECT * FROM downloads ORDER BY createdAt DESC";
    return FlowUtil.createFlow(__db, false, new String[] {"downloads"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfUrl = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "url");
        final int _columnIndexOfTitle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "title");
        final int _columnIndexOfMime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "mime");
        final int _columnIndexOfSource = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "source");
        final int _columnIndexOfFormatId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "formatId");
        final int _columnIndexOfIsAudioOnly = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "isAudioOnly");
        final int _columnIndexOfStatus = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "status");
        final int _columnIndexOfProgress = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "progress");
        final int _columnIndexOfSpeedBps = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "speedBps");
        final int _columnIndexOfTotalBytes = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "totalBytes");
        final int _columnIndexOfDownloadedBytes = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "downloadedBytes");
        final int _columnIndexOfLocalPath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "localPath");
        final int _columnIndexOfErrorMsg = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "errorMsg");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "createdAt");
        final int _columnIndexOfUpdatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "updatedAt");
        final List<DownloadEntity> _result = new ArrayList<DownloadEntity>();
        while (_stmt.step()) {
          final DownloadEntity _item;
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          final String _tmpUrl;
          if (_stmt.isNull(_columnIndexOfUrl)) {
            _tmpUrl = null;
          } else {
            _tmpUrl = _stmt.getText(_columnIndexOfUrl);
          }
          final String _tmpTitle;
          if (_stmt.isNull(_columnIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = _stmt.getText(_columnIndexOfTitle);
          }
          final String _tmpMime;
          if (_stmt.isNull(_columnIndexOfMime)) {
            _tmpMime = null;
          } else {
            _tmpMime = _stmt.getText(_columnIndexOfMime);
          }
          final String _tmpSource;
          if (_stmt.isNull(_columnIndexOfSource)) {
            _tmpSource = null;
          } else {
            _tmpSource = _stmt.getText(_columnIndexOfSource);
          }
          final String _tmpFormatId;
          if (_stmt.isNull(_columnIndexOfFormatId)) {
            _tmpFormatId = null;
          } else {
            _tmpFormatId = _stmt.getText(_columnIndexOfFormatId);
          }
          final boolean _tmpIsAudioOnly;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfIsAudioOnly));
          _tmpIsAudioOnly = _tmp != 0;
          final String _tmpStatus;
          if (_stmt.isNull(_columnIndexOfStatus)) {
            _tmpStatus = null;
          } else {
            _tmpStatus = _stmt.getText(_columnIndexOfStatus);
          }
          final int _tmpProgress;
          _tmpProgress = (int) (_stmt.getLong(_columnIndexOfProgress));
          final long _tmpSpeedBps;
          _tmpSpeedBps = _stmt.getLong(_columnIndexOfSpeedBps);
          final long _tmpTotalBytes;
          _tmpTotalBytes = _stmt.getLong(_columnIndexOfTotalBytes);
          final long _tmpDownloadedBytes;
          _tmpDownloadedBytes = _stmt.getLong(_columnIndexOfDownloadedBytes);
          final String _tmpLocalPath;
          if (_stmt.isNull(_columnIndexOfLocalPath)) {
            _tmpLocalPath = null;
          } else {
            _tmpLocalPath = _stmt.getText(_columnIndexOfLocalPath);
          }
          final String _tmpErrorMsg;
          if (_stmt.isNull(_columnIndexOfErrorMsg)) {
            _tmpErrorMsg = null;
          } else {
            _tmpErrorMsg = _stmt.getText(_columnIndexOfErrorMsg);
          }
          final long _tmpCreatedAt;
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt);
          final long _tmpUpdatedAt;
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt);
          _item = new DownloadEntity(_tmpId,_tmpUrl,_tmpTitle,_tmpMime,_tmpSource,_tmpFormatId,_tmpIsAudioOnly,_tmpStatus,_tmpProgress,_tmpSpeedBps,_tmpTotalBytes,_tmpDownloadedBytes,_tmpLocalPath,_tmpErrorMsg,_tmpCreatedAt,_tmpUpdatedAt);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Object snapshot(final Continuation<? super List<DownloadEntity>> $completion) {
    final String _sql = "SELECT * FROM downloads ORDER BY createdAt DESC";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfUrl = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "url");
        final int _columnIndexOfTitle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "title");
        final int _columnIndexOfMime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "mime");
        final int _columnIndexOfSource = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "source");
        final int _columnIndexOfFormatId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "formatId");
        final int _columnIndexOfIsAudioOnly = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "isAudioOnly");
        final int _columnIndexOfStatus = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "status");
        final int _columnIndexOfProgress = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "progress");
        final int _columnIndexOfSpeedBps = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "speedBps");
        final int _columnIndexOfTotalBytes = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "totalBytes");
        final int _columnIndexOfDownloadedBytes = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "downloadedBytes");
        final int _columnIndexOfLocalPath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "localPath");
        final int _columnIndexOfErrorMsg = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "errorMsg");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "createdAt");
        final int _columnIndexOfUpdatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "updatedAt");
        final List<DownloadEntity> _result = new ArrayList<DownloadEntity>();
        while (_stmt.step()) {
          final DownloadEntity _item;
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          final String _tmpUrl;
          if (_stmt.isNull(_columnIndexOfUrl)) {
            _tmpUrl = null;
          } else {
            _tmpUrl = _stmt.getText(_columnIndexOfUrl);
          }
          final String _tmpTitle;
          if (_stmt.isNull(_columnIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = _stmt.getText(_columnIndexOfTitle);
          }
          final String _tmpMime;
          if (_stmt.isNull(_columnIndexOfMime)) {
            _tmpMime = null;
          } else {
            _tmpMime = _stmt.getText(_columnIndexOfMime);
          }
          final String _tmpSource;
          if (_stmt.isNull(_columnIndexOfSource)) {
            _tmpSource = null;
          } else {
            _tmpSource = _stmt.getText(_columnIndexOfSource);
          }
          final String _tmpFormatId;
          if (_stmt.isNull(_columnIndexOfFormatId)) {
            _tmpFormatId = null;
          } else {
            _tmpFormatId = _stmt.getText(_columnIndexOfFormatId);
          }
          final boolean _tmpIsAudioOnly;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfIsAudioOnly));
          _tmpIsAudioOnly = _tmp != 0;
          final String _tmpStatus;
          if (_stmt.isNull(_columnIndexOfStatus)) {
            _tmpStatus = null;
          } else {
            _tmpStatus = _stmt.getText(_columnIndexOfStatus);
          }
          final int _tmpProgress;
          _tmpProgress = (int) (_stmt.getLong(_columnIndexOfProgress));
          final long _tmpSpeedBps;
          _tmpSpeedBps = _stmt.getLong(_columnIndexOfSpeedBps);
          final long _tmpTotalBytes;
          _tmpTotalBytes = _stmt.getLong(_columnIndexOfTotalBytes);
          final long _tmpDownloadedBytes;
          _tmpDownloadedBytes = _stmt.getLong(_columnIndexOfDownloadedBytes);
          final String _tmpLocalPath;
          if (_stmt.isNull(_columnIndexOfLocalPath)) {
            _tmpLocalPath = null;
          } else {
            _tmpLocalPath = _stmt.getText(_columnIndexOfLocalPath);
          }
          final String _tmpErrorMsg;
          if (_stmt.isNull(_columnIndexOfErrorMsg)) {
            _tmpErrorMsg = null;
          } else {
            _tmpErrorMsg = _stmt.getText(_columnIndexOfErrorMsg);
          }
          final long _tmpCreatedAt;
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt);
          final long _tmpUpdatedAt;
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt);
          _item = new DownloadEntity(_tmpId,_tmpUrl,_tmpTitle,_tmpMime,_tmpSource,_tmpFormatId,_tmpIsAudioOnly,_tmpStatus,_tmpProgress,_tmpSpeedBps,_tmpTotalBytes,_tmpDownloadedBytes,_tmpLocalPath,_tmpErrorMsg,_tmpCreatedAt,_tmpUpdatedAt);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM downloads WHERE id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM downloads";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object updateProgress(final long id, final String status, final int progress,
      final long speedBps, final long downloaded, final long total, final long ts,
      final Continuation<? super Unit> $completion) {
    final String _sql = "UPDATE downloads SET status = ?, progress = ?, speedBps = ?, downloadedBytes = ?, totalBytes = ?, updatedAt = ? WHERE id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (status == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, status);
        }
        _argIndex = 2;
        _stmt.bindLong(_argIndex, progress);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, speedBps);
        _argIndex = 4;
        _stmt.bindLong(_argIndex, downloaded);
        _argIndex = 5;
        _stmt.bindLong(_argIndex, total);
        _argIndex = 6;
        _stmt.bindLong(_argIndex, ts);
        _argIndex = 7;
        _stmt.bindLong(_argIndex, id);
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object markCompleted(final long id, final String status, final String localPath,
      final long ts, final Continuation<? super Unit> $completion) {
    final String _sql = "UPDATE downloads SET status = ?, localPath = ?, progress = 100, updatedAt = ? WHERE id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (status == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, status);
        }
        _argIndex = 2;
        if (localPath == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, localPath);
        }
        _argIndex = 3;
        _stmt.bindLong(_argIndex, ts);
        _argIndex = 4;
        _stmt.bindLong(_argIndex, id);
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object markFailed(final long id, final String error, final long ts,
      final Continuation<? super Unit> $completion) {
    final String _sql = "UPDATE downloads SET status = 'failed', errorMsg = ?, updatedAt = ? WHERE id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (error == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, error);
        }
        _argIndex = 2;
        _stmt.bindLong(_argIndex, ts);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, id);
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
