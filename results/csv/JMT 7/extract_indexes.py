import pandas as pd
import numpy as np
import os
import sys


def calcola_statistiche(filename, is_throughput=False):
    """Calcola media pesata, IC 95% ed Errore Relativo da un file CSV di JMT.
    
    Se is_throughput=True, il file contiene inter-departure times (secondi tra
    una partenza e l'altra); il throughput e' il RECIPROCO della media, quindi
    si applica la trasformazione 1/x prima di restituire i risultati.
    """
    if not os.path.exists(filename):
        return None, None, None, None
    
    try:
        df = pd.read_csv(filename, sep=None, engine='python')
        df = df.dropna(subset=['SAMPLE', 'WEIGHT'])
        
        samples = df['SAMPLE'].values
        weights = df['WEIGHT'].values
        
        mean = np.average(samples, weights=weights)
        variance = np.average((samples - mean)**2, weights=weights)
        n = len(samples)
        sem = np.sqrt(variance / n)
        
        z_score = 1.96 # Per IC al 95%
        margin_of_error = z_score * sem
        
        if is_throughput:
            # I SAMPLE sono inter-departure times: throughput = 1 / mean.
            # Propagazione errore: se f(x)=1/x, allora sigma_f = sigma_x / x^2
            # IC: [1/(mean+margin), 1/(mean-margin)] -- nota inversione bounds
            throughput_mean = 1.0 / mean
            # propago l'incertezza: delta_X = margin_of_error => delta_throughput = margin_of_error / mean^2
            throughput_margin = margin_of_error / (mean ** 2)
            ic_lower = throughput_mean - throughput_margin
            ic_upper = throughput_mean + throughput_margin
            re = (throughput_margin / abs(throughput_mean)) * 100
            return throughput_mean, ic_lower, ic_upper, re
        else:
            ic_lower = mean - margin_of_error
            ic_upper = mean + margin_of_error
            re = (margin_of_error / abs(mean)) * 100
            return mean, ic_lower, ic_upper, re
    except Exception:
        return None, None, None, None

def crea_riga(nome, stats):
    """Formatta i risultati in un dizionario pronto per il DataFrame."""
    mean, ic_l, ic_u, re = stats
    if mean is None:
        return {'Metrica': nome, 'Media': 'File non trovato', 'IC_Lower': 'N/A', 'IC_Upper': 'N/A', 'RE(%)': 'N/A'}
    return {
        'Metrica': nome, 
        'Media': round(mean, 4), 
        'IC_Lower': round(ic_l, 4), 
        'IC_Upper': round(ic_u, 4), 
        'RE(%)': round(re, 4)
    }

def main():
    print("Inizio calcolo metriche dai file CSV di JMT...")
    if len(sys.argv) < 2:
        print("Devi passare un numero intero come argomento.")
        sys.exit(1)

    lambda_open = int(sys.argv[1])   # conversione da stringa a intero
    print(f"Hai passato il numero: {lambda_open}")
    # --- 1. THROUGHPUT ---
    throughput_data = [
        crea_riga("X_sys_chiuso", calcola_statistiche(f'{lambda_open}/Customer Closed_System Throughput.csv', is_throughput=True)),
        crea_riga("X_Q1_tot", calcola_statistiche(f'{lambda_open}/Q1_Throughput.csv', is_throughput=True)),
        crea_riga("X_Q1_chiuso", calcola_statistiche(f'{lambda_open}/Q1_Customer Closed_Throughput.csv', is_throughput=True)),
        crea_riga("X_Q1_aperto", calcola_statistiche(f'{lambda_open}/Q1_Customer Open_Throughput.csv', is_throughput=True)),
        crea_riga("X_Q2", calcola_statistiche(f'{lambda_open}/Q2_Customer Closed_Throughput.csv', is_throughput=True))
    ]
    pd.DataFrame(throughput_data).to_csv(f'{lambda_open}/risultati_1_throughput.csv', index=False)
    print("Salvato: risultati_1_throughput.csv")

    # --- 2. UTILIZZO (RHO) ---
    rho_q1_stats = calcola_statistiche(f'{lambda_open}/Q1_Utilization.csv')
    rho_q2_stats = calcola_statistiche(f'{lambda_open}/Q2_Customer Closed_Utilization.csv')
    
    rho_data = [
        crea_riga("Rho_Q1_tot", rho_q1_stats),
        crea_riga("Rho_Q2", rho_q2_stats)
    ]
    pd.DataFrame(rho_data).to_csv(f'{lambda_open}/risultati_2_utilizzazione.csv', index=False)
    print("Salvato: risultati_2_utilizzazione.csv")

    # --- 3. TEMPI DI RISPOSTA (E[T]) ---
    t_sys_tot_stats = calcola_statistiche(f'{lambda_open}/Customer Closed_System Response Time.csv')
    t_q0_stats = calcola_statistiche(f'{lambda_open}/Q0_Customer Closed_Response Time.csv')
    
    # Calcolo Derivato: Sistema Centrale = T_sys_totale - T_Q0
    t_sys_centrale_val = 'N/A'
    if t_sys_tot_stats[0] is not None and t_q0_stats[0] is not None:
        t_sys_centrale_val = round(t_sys_tot_stats[0] - t_q0_stats[0], 4)

    t_data = [
        crea_riga("E[T1]_chiuso", calcola_statistiche(f'{lambda_open}/Q1_Customer Closed_Response Time.csv')),
        crea_riga("E[T1]_aperto", calcola_statistiche(f'{lambda_open}/Q1_Customer Open_Response Time.csv')),
        crea_riga("E[T2]", calcola_statistiche(f'{lambda_open}/Q2_Customer Closed_Response Time.csv')),
        crea_riga("E[T_sys]_totale_con_Q0", t_sys_tot_stats),
        {'Metrica': 'E[T_sys]_centrale_derivato', 'Media': t_sys_centrale_val, 'IC_Lower': 'N/A', 'IC_Upper': 'N/A', 'RE(%)': 'N/A'}
    ]
    pd.DataFrame(t_data).to_csv(f'{lambda_open}/risultati_3_tempi_risposta.csv', index=False)
    print("Salvato: risultati_3_tempi_risposta.csv")

    # --- 4. LUNGHEZZA CODE (E[Nq]) ---
    n_q1_stats = calcola_statistiche(f'{lambda_open}/Q1_Number of Customers.csv')
    n_q2_stats = calcola_statistiche(f'{lambda_open}/Q2_Customer Closed_Number of Customers.csv')

    # Calcolo Derivato: E[Nq] = E[N] - Rho
    nq1_val, nq2_val = 'N/A', 'N/A'
    if n_q1_stats[0] is not None and rho_q1_stats[0] is not None:
        nq1_val = round(n_q1_stats[0] - rho_q1_stats[0], 4)
    if n_q2_stats[0] is not None and rho_q2_stats[0] is not None:
        nq2_val = round(n_q2_stats[0] - rho_q2_stats[0], 4)

    nq_data = [
        {'Metrica': 'E[Nq1]_tot_derivato', 'Media': nq1_val, 'IC_Lower': 'N/A', 'IC_Upper': 'N/A', 'RE(%)': 'N/A'},
        {'Metrica': 'E[Nq2]_tot_derivato', 'Media': nq2_val, 'IC_Lower': 'N/A', 'IC_Upper': 'N/A', 'RE(%)': 'N/A'}
    ]
    pd.DataFrame(nq_data).to_csv(f'{lambda_open}/risultati_4_lunghezza_code.csv', index=False)
    print("Salvato: risultati_4_lunghezza_code.csv")
    
    print("\nTutti i file CSV sono stati generati con successo!")

if __name__ == "__main__":
    main()