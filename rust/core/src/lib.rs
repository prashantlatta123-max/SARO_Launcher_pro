/// Normalize a search term for deterministic local matching.
/// Unicode-aware collation can be added behind this API in a later release.
pub fn normalize_ascii_case(input: &str) -> String {
    input.trim().to_lowercase()
}

/// Return the original label indexes matching a query.
pub fn search_indexes(labels: &[String], query: &str) -> Vec<usize> {
    let needle = normalize_ascii_case(query);
    if needle.is_empty() {
        return (0..labels.len()).collect();
    }
    labels.iter().enumerate()
        .filter_map(|(index, label)| {
            normalize_ascii_case(label).contains(&needle).then_some(index)
        })
        .collect()
}

/// Deterministic case-insensitive sort, preserving original label text.
pub fn sort_labels(labels: &mut [String]) {
    labels.sort_by(|a, b| {
        normalize_ascii_case(a)
            .cmp(&normalize_ascii_case(b))
            .then_with(|| a.cmp(b))
    });
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn search_matches_without_case_sensitivity() {
        let labels = vec!["Camera".into(), "Settings".into(), "Calendar".into()];
        assert_eq!(search_indexes(&labels, "cam"), vec![0]);
    }

    #[test]
    fn empty_query_returns_every_index() {
        let labels = vec!["A".into(), "B".into()];
        assert_eq!(search_indexes(&labels, ""), vec![0, 1]);
    }

    #[test]
    fn sorting_is_deterministic() {
        let mut labels = vec!["zebra".into(), "Alpha".into(), "alpha".into()];
        sort_labels(&mut labels);
        assert_eq!(labels, vec!["Alpha", "alpha", "zebra"]);
    }
}
