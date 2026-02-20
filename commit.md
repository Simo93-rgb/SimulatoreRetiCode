docs(punto5): Rewrite as academic report with full data and analysis

Complete restructuring of punto5.md following the academic style of
punto4.md. The document is now a self-contained report covering:

1. M/M/1 Validation (Exp 1-3):
   - Three load levels: ρ=0.5, 0.8, 0.9
   - Full tables: Simulator (IC 95%) vs JMT (IC 95%) vs Theory
   - All Δ% < 2% for ρ≤0.8, Δ% < 4.5% for ρ=0.9
   - E[Nq] derived via Little's Law (W×X) for JMT data

2. Service Variability Impact (Exp 4-6):
   - Fixed: λ=0.8, E[S]=1.0, ρ=0.8 — only distribution changes
   - M/D/1 (Cs²≈0): E[T]=2.987s — Δ vs M/M/1: -40%
   - M/E4/1 (Cs²=0.25): E[T]=3.492s — Δ vs M/M/1: -30%
   - M/H2/1 (Cs²>1): E[T]=5.990s — Δ vs M/M/1: +20%

3. Comparative Analysis:
   - Summary table: Cs² vs E[T] for all 4 distributions
   - Mermaid xychart: E[T] vs Cs² (line chart)
   - Pollaczek-Khinchine formula derived and verified:
     E[Nq] = ρ²(1+Cs²) / 2(1-ρ)
     Experimental Δ < 1% for distributions with known Cs²
   - P-K inserted to show explicit dependence on Cs²
     (not just as formula but as validation tool)

4. Statistical Methodology:
   - Independent replications vs batch means comparison
   - Effective sample size analysis: n_eff ≈ n/9 for JMT with ρ=0.8

5. Conclusions (5 points):
   - Validation confirmed across all load levels
   - 100% E[T] spread from Cs²=0 to Cs²>1
   - P-K verified with Δ<1%
   - Little's Law usage justified
   - Key insight: λ, μ, ρ alone insufficient to predict E[T]
