# Inscribed 0.7.3

This source update publishes the guide-book revision already used by the local 0.7.3
modpack build. It does not change the canonical recipe/loot datapack or ritual rules.

- Guide navigation now keeps back/forward history, including selection, query, filters,
  paging and scroll state. Restored selections are resolved against the current snapshot.
- The guide and altar inspection use the updated book geometry, paper theme and responsive
  panels. Ribbon placement and footer navigation follow the book bounds.
- English and Brazilian Portuguese labels include the new navigation actions.
- The publication checkout removes an unused old LSBC ribbon shim absent from the working
  0.7.3 source. The optional knowledge integration remains in place.

Recipe IDs, reagent counts, physical-library requirements, extraction behavior and the
20% default extraction failure remain unchanged. Historical 0.7.2/RC evidence is retained
under its original version names. Build the current source as documented in
[INSCRIBED_DEV_SETUP.md](INSCRIBED_DEV_SETUP.md); local dependency JARs are not vendored.
