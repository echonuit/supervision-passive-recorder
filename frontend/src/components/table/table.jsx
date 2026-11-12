import React, { useEffect, useState } from 'react';
import './table.css';

export default function TableView() {
  const [sensors, setSensors] = useState([]);
  const [lastUpdate, setLastUpdate] = useState(new Date());

  const fetchSensors = () => {
    fetch('/api/table/sensors')
      .then(res => {
        if (!res.ok) throw new Error('Non autorisé ou erreur serveur');
        return res.json();
      })
      .then(data => {
        setSensors(data);
        setLastUpdate(new Date());
      })
      .catch(err => console.error("Erreur de synchronisation des capteurs :", err));
  };

  useEffect(() => {
    fetchSensors();
    const interval = setInterval(fetchSensors, 5000);
    return () => clearInterval(interval);
  }, []);

  const criticalSensors = sensors.filter(s => s.battery < 20 || s.storage < 15);
  const unassignedSensors = sensors.filter(s => !s.searchName);

  const searchesMap = sensors.reduce((acc, sensor) => {
    if (sensor.searchName) {
      if (!acc[sensor.searchName]) {
        acc[sensor.searchName] = [];
      }
      acc[sensor.searchName].push(sensor);
    }
    return acc;
  }, {});

  const renderStatusBadge = (status) => {
    switch (status) {
      case 'Stable': return <span className="status-stable">Stable</span>;
      case 'Avertissement': return <span className="status-warning">Avertissement</span>;
      case 'Critique': return <span className="status-critical">Critique</span>;
      default: return <span>{status}</span>;
    }
  };

  return (
    <div className="table-view-container">
      <div className="table-view-header">
        <h1>VUE D'ENSEMBLE</h1>
        <span className="last-update">
          Mis à jour le : {lastUpdate.toLocaleDateString()} à {lastUpdate.toLocaleTimeString()}
        </span>
      </div>

      {criticalSensors.length > 0 && (
        <div className="table-card critical-card">
          <div className="table-card-header critical-header">
            <span>🚨 ALERTES CRITIQUES & PROBLÈMES URGENTS</span>
            <span className="badge-count">{criticalSensors.length} capteur(s)</span>
          </div>
          <SensorTable sensors={criticalSensors} renderStatusBadge={renderStatusBadge} />
        </div>
      )}

      {Object.entries(searchesMap).map(([searchName, groupSensors]) => (
        <div key={searchName} className="table-card">
          <div className="table-card-header">
            <h2>{searchName}</h2>
          </div>
          <SensorTable sensors={groupSensors} renderStatusBadge={renderStatusBadge} />
        </div>
      ))}

      {unassignedSensors.length > 0 && (
        <div className="table-card">
          <div className="table-card-header">
            <h2>Capteurs non assignés</h2>
          </div>
          <SensorTable sensors={unassignedSensors} renderStatusBadge={renderStatusBadge} />
        </div>
      )}
    </div>
  );
}

function SensorTable({ sensors, renderStatusBadge }) {
  return (
    <table className="sensor-table">
      <thead>
        <tr>
          <th>Nom du recorder</th>
          <th>Stockage</th>
          <th>Batterie</th>
          <th>État système</th>
          <th>Dernière synchro</th>
          <th>Alertes / Problèmes</th>
          <th className="text-right">Actions</th>
        </tr>
      </thead>
      <tbody>
        {sensors.map(sensor => (
          <tr key={sensor.id}>
            <td className="font-bold">{sensor.name}</td>
            <td>{sensor.storage}%</td>
            <td>
              <div className="battery-cell">
                <div className="battery-bar">
                  <div
                    className={`battery-fill ${sensor.battery < 20 ? 'low' : 'ok'}`}
                    style={{ width: `${sensor.battery}%` }}
                  ></div>
                </div>
                <span>{sensor.battery}%</span>
              </div>
            </td>
            <td>{renderStatusBadge(sensor.status)}</td>
            <td className="text-muted">{sensor.lastSync}</td>
            <td className="text-alert">{sensor.alert}</td>
            <td className="text-right">
              <button
                onClick={() => alert(`Capteur : ${sensor.name} (DevEUI: ${sensor.devEUI})`)}
                className="btn-detail"
              >
                DÉTAIL
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}