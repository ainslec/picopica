# Contributing to Picopica

Thank you for your interest in contributing to **PicoPica**!  
We welcome bug reports, feature requests, documentation improvements, and code contributions.

---

### 📜 Contributor Assignment Agreement (CAA)

All contributions require acceptance of the
[Contributor Assignment Agreement](CAA.md).

By submitting a pull request, you confirm that you have read and agreed to the terms of the CAA.  

If this is your first contribution, GitHub will prompt you (via our CLA bot) to sign electronically before your PR can be merged.

---

### 🛠 How to Contribute

#### 1. Reporting Issues
- Use the [GitHub Issues](../../issues) page to report bugs or request features.
- Please include as much detail as possible (error messages, stack traces, version info, reproduction steps).

#### 2. Submitting Changes
- Fork the repository and create a feature branch:

```bash
  git checkout -b feature/my-new-feature
```

* Make your changes and add tests where appropriate.

* Ensure that the code builds and all tests pass:

```bash
  mvn clean verify
```

* Commit your changes with a clear message:

```bash
git commit -s -m "Add support for XYZ feature"
```

  (The `-s` flag adds a Signed-off-by line, which helps track agreement.)

* Push your branch and open a Pull Request.

#### 3. Code Style

* Follow existing code formatting and conventions.
* Use meaningful variable/method names.
* Keep commits focused and small where possible.

---

### ✅ Checklist Before Submitting

* [ ] My code builds successfully with `mvn clean verify`.
* [ ] I have added/updated tests for my changes.
* [ ] I have read and agreed to the [CAA](CAA.md).
* [ ] I have signed the CLA via the GitHub bot (first-time contributors only).

---

### 💬 Questions?

If you’re unsure about anything, feel free to open a discussion or reach out via the issue tracker.