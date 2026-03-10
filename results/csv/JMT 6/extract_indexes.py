import pandas as pd
import numpy as np
import os

# Sposta la working directory nella cartella dello script,
# così i path relativi ai CSV funzionano indipendentemente da dove si lancia.
os.chdir(os.path.dirname(os.path.abspath(__file__)))

# N corrispondente a ciascun indice file (1..5)
ESPERIMENTI = {1: 5, 2: 11, 3: 17, 4: 23, 5: 30}


def calcola_statistiche(filename, is_throughput=False):
    """Calcola media pesata, IC 95% ed Errore Relativo da un file CSV di JMT.

    Se is_throughput=True, i SAMPLE sono inter-departure times (secondi tra
    una partenza e l'altra); il throughput e' il RECIPROCO della media, quindi
    si applica la trasformazione 1/x e si propaga l'errore.
    """
    if not os.path.exists(filename):
        print(f"  ATTENZIONE: file non trovato '{filename}'")
        return None, None, None, None

    try:
        df = pd.read_csv(filename).dropna(subset=['SAMPLE', 'WEIGHT'])

        samples = df['SAMPLE'].values
        weights = df['WEIGHT'].values

        mean = np.average(samples, weights=weights)
        variance = np.average((samples - mean) ** 2, weights=weights)
        n = len(samples)
        sem = np.sqrt(variance / n)

        z_score = 1.96  # IC al 95%
        margin_of_error = z_score * sem

        if is_throughput:
            # SAMPLE = inter-departure time => throughput = 1 / mean
            # Propagazione errore: delta_X = margin / mean^2
            throughput_mean = 1.0 / mean
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
    except Exception as e:
        print(f"  ERRORE su '{filename}': {e}")
        return None, None, None, None


def crea_riga(n_val, stats):
    """Formatta i risultati in un dizionario con N come chiave riga."""
    mean, ic_l, ic_u, re = stats
    if mean is None:
        return {'N': n_val, 'Media': 'N/A', 'IC_Lower': 'N/A', 'IC_Upper': 'N/A', 'RE(%)': 'N/A'}
    return {
        'N': n_val,
        'Media': round(mean, 4),
        'IC_Lower': round(ic_l, 4),
        'IC_Upper': round(ic_u, 4),
        'RE(%)': round(re, 4),
    }


def main():
    print("Inizio calcolo metriche dai file CSV di JMT...")
    print("(5 esperimenti: N = 5, 11, 17, 23, 30)\n")

    throughput_rows = {'X_sys': [], 'X_Q1': [], 'X_Q2': []}
    rho_rows        = {'Rho_Q1': [], 'Rho_Q2': []}
    t_rows          = {'E[T1]': [], 'E[T2]': [], 'E[T_sys]_con_Q0': [], 'E[T_sys]_centrale': []}
    nq_rows         = {'E[Nq1]': [], 'E[Nq2]': []}

    for idx, n_val in ESPERIMENTI.items():
        print(f"--- Esperimento {idx}: N={n_val} ---")

        # Throughput (inter-departure times => is_throughput=True)
        x_sys = calcola_statistiche(f'System Throughput_{idx}.csv',    is_throughput=True)
        x_q1  = calcola_statistiche(f'Q1_Throughput_{idx}.csv',        is_throughput=True)
        x_q2  = calcola_statistiche(f'Q2_Throughput_{idx}.csv',        is_throughput=True)
        throughput_rows['X_sys'].append(crea_riga(n_val, x_sys))
        throughput_rows['X_Q1'].append(crea_riga(n_val, x_q1))
        throughput_rows['X_Q2'].append(crea_riga(n_val, x_q2))

        # Utilizzo
        rho_q1 = calcola_statistiche(f'Q1_Utilization_{idx}.csv')
        rho_q2 = calcola_statistiche(f'Q2_Utilization_{idx}.csv')
        rho_rows['Rho_Q1'].append(crea_riga(n_val, rho_q1))
        rho_rows['Rho_Q2'].append(crea_riga(n_val, rho_q2))

        # Tempi di risposta
        t_q1      = calcola_statistiche(f'Q1_Response Time_{idx}.csv')
        t_q2      = calcola_statistiche(f'Q2_Response Time_{idx}.csv')
        t_sys_tot = calcola_statistiche(f'System Response Time_{idx}.csv')
        t_q0      = calcola_statistiche(f'Q0_Response Time_{idx}.csv')

        t_centrale_val = 'N/A'
        if t_sys_tot[0] is not None and t_q0[0] is not None:
            t_centrale_val = round(t_sys_tot[0] - t_q0[0], 4)

        t_rows['E[T1]'].append(crea_riga(n_val, t_q1))
        t_rows['E[T2]'].append(crea_riga(n_val, t_q2))
        t_rows['E[T_sys]_con_Q0'].append(crea_riga(n_val, t_sys_tot))
        t_rows['E[T_sys]_centrale'].append(
            {'N': n_val, 'Media': t_centrale_val, 'IC_Lower': 'N/A', 'IC_Upper': 'N/A', 'RE(%)': 'N/A'}
        )

        # Lunghezza code: E[Nq] = E[N] - Rho
        n_q1 = calcola_statistiche(f'Q1_Number of Customers_{idx}.csv')
        n_q2 = calcola_statistiche(f'Q2_Number of Customers_{idx}.csv')

        nq1_val = round(n_q1[0] - rho_q1[0], 4) if (n_q1[0] is not None and rho_q1[0] is not None) else 'N/A'
        nq2_val = round(n_q2[0] - rho_q2[0], 4) if (n_q2[0] is not None and rho_q2[0] is not None) else 'N/A'
        nq_rows['E[Nq1]'].append({'N': n_val, 'Media': nq1_val, 'IC_Lower': 'N/A', 'IC_Upper': 'N/A', 'RE(%)': 'N/A'})
        nq_rows['E[Nq2]'].append({'N': n_val, 'Media': nq2_val, 'IC_Lower': 'N/A', 'IC_Upper': 'N/A', 'RE(%)': 'N/A'})

    # --- Salvataggio ---
    def salva(nome_file, rows_dict):
        frames = []
        for metrica, righe in rows_dict.items():
            df = pd.DataFrame(righe)
            df.insert(0, 'Metrica', metrica)
            frames.append(df)
        pd.concat(frames, ignore_index=True).to_csv(nome_file, index=False)
        print(f"Salvato: {nome_file}")

    print()
    salva('risultati_1_throughput.csv',    throughput_rows)
    salva('risultati_2_utilizzazione.csv', rho_rows)
    salva('risultati_3_tempi_risposta.csv', t_rows)
    salva('risultati_4_lunghezza_code.csv', nq_rows)

    print("\nTutti i file CSV sono stati generati con successo!")


if __name__ == "__main__":
    main()