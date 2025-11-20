-- 创建空闲监控配置表
CREATE TABLE IF NOT EXISTS idle_monitor_configs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id INTEGER NOT NULL,
    config_name VARCHAR(100) NOT NULL,
    work_time_slots TEXT NOT NULL,
    idle_threshold_minutes INTEGER NOT NULL,
    monitored_actions TEXT NOT NULL,
    exclude_holidays BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_by INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_idle_config_tenant ON idle_monitor_configs(tenant_id);
CREATE INDEX IF NOT EXISTS idx_idle_config_active ON idle_monitor_configs(is_active);

-- 创建催员空闲记录表
CREATE TABLE IF NOT EXISTS collector_idle_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id INTEGER NOT NULL,
    collector_id INTEGER NOT NULL,
    agency_id INTEGER NOT NULL,
    team_id INTEGER NOT NULL,
    idle_date DATE NOT NULL,
    idle_start_time TIMESTAMP NOT NULL,
    idle_end_time TIMESTAMP NOT NULL,
    idle_duration_minutes INTEGER NOT NULL,
    before_action TEXT,
    after_action TEXT,
    config_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (collector_id) REFERENCES collectors(id),
    FOREIGN KEY (agency_id) REFERENCES collection_agencies(id),
    FOREIGN KEY (team_id) REFERENCES collection_teams(id),
    FOREIGN KEY (config_id) REFERENCES idle_monitor_configs(id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_idle_record_tenant ON collector_idle_records(tenant_id);
CREATE INDEX IF NOT EXISTS idx_idle_record_collector ON collector_idle_records(collector_id);
CREATE INDEX IF NOT EXISTS idx_idle_record_date ON collector_idle_records(idle_date);
CREATE INDEX IF NOT EXISTS idx_idle_record_agency ON collector_idle_records(agency_id);
CREATE INDEX IF NOT EXISTS idx_idle_record_team ON collector_idle_records(team_id);

-- 创建催员空闲统计表
CREATE TABLE IF NOT EXISTS collector_idle_stats (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id INTEGER NOT NULL,
    collector_id INTEGER NOT NULL,
    agency_id INTEGER NOT NULL,
    team_id INTEGER NOT NULL,
    stat_date DATE NOT NULL,
    idle_count INTEGER DEFAULT 0,
    total_idle_minutes INTEGER DEFAULT 0,
    longest_idle_minutes INTEGER DEFAULT 0,
    avg_idle_minutes DECIMAL(10, 2) DEFAULT 0,
    work_minutes INTEGER DEFAULT 0,
    idle_rate DECIMAL(5, 4) DEFAULT 0,
    managed_cases_total INTEGER DEFAULT 0,
    managed_cases_collected INTEGER DEFAULT 0,
    managed_amount_total DECIMAL(20, 2) DEFAULT 0,
    managed_amount_collected DECIMAL(20, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (collector_id) REFERENCES collectors(id),
    FOREIGN KEY (agency_id) REFERENCES collection_agencies(id),
    FOREIGN KEY (team_id) REFERENCES collection_teams(id),
    UNIQUE(collector_id, stat_date)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_idle_stats_tenant ON collector_idle_stats(tenant_id);
CREATE INDEX IF NOT EXISTS idx_idle_stats_collector ON collector_idle_stats(collector_id);
CREATE INDEX IF NOT EXISTS idx_idle_stats_date ON collector_idle_stats(stat_date);
CREATE INDEX IF NOT EXISTS idx_idle_stats_agency ON collector_idle_stats(agency_id);
CREATE INDEX IF NOT EXISTS idx_idle_stats_team ON collector_idle_stats(team_id);

-- 显示创建结果
SELECT '✅ 空闲监控数据表创建成功！' as result;

