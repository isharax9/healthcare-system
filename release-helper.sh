#!/bin/bash

# GlobeMed Healthcare System Release Helper
# This script helps create properly formatted commits for automated releases

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_color() {
    printf "${1}${2}${NC}\n"
}

print_header() {
    echo
    print_color $BLUE "🏥 GlobeMed Healthcare System - Release Helper"
    echo "=============================================="
    echo
}

show_help() {
    print_header
    echo "This script helps you create conventional commits for automated releases."
    echo
    print_color $YELLOW "Usage:"
    echo "  ./release-helper.sh [command]"
    echo
    print_color $YELLOW "Commands:"
    echo "  commit      - Create a conventional commit"
    echo "  status      - Show current version and pending changes"
    echo "  changelog   - View recent changelog entries"
    echo "  help        - Show this help message"
    echo
    print_color $YELLOW "Commit Types:"
    echo "  feat      - New feature (minor version bump)"
    echo "  fix       - Bug fix (patch version bump)"
    echo "  docs      - Documentation changes"
    echo "  style     - Code style changes"
    echo "  refactor  - Code refactoring"
    echo "  perf      - Performance improvements"
    echo "  test      - Test changes"
    echo "  build     - Build system changes"
    echo "  ci        - CI configuration changes"
    echo "  chore     - Other changes"
    echo
    print_color $GREEN "Examples:"
    echo "  feat(auth): add OAuth2 authentication"
    echo "  fix(database): resolve connection timeout"
    echo "  docs: update installation guide"
    echo "  feat!: redesign user API (breaking change)"
    echo
}

get_current_version() {
    grep -E '<version>' pom.xml | head -1 | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d ' '
}

create_commit() {
    print_header
    print_color $GREEN "Current version: $(get_current_version)"
    echo
    
    # Get commit type
    print_color $YELLOW "Select commit type:"
    echo "1) feat      - New feature (minor version bump)"
    echo "2) fix       - Bug fix (patch version bump)"
    echo "3) docs      - Documentation changes"
    echo "4) style     - Code style changes"
    echo "5) refactor  - Code refactoring"
    echo "6) perf      - Performance improvements"
    echo "7) test      - Test changes"
    echo "8) build     - Build system changes"
    echo "9) ci        - CI configuration changes"
    echo "10) chore    - Other changes"
    echo
    read -p "Enter choice (1-10): " choice
    
    case $choice in
        1) type="feat" ;;
        2) type="fix" ;;
        3) type="docs" ;;
        4) type="style" ;;
        5) type="refactor" ;;
        6) type="perf" ;;
        7) type="test" ;;
        8) type="build" ;;
        9) type="ci" ;;
        10) type="chore" ;;
        *) print_color $RED "Invalid choice"; exit 1 ;;
    esac
    
    # Get scope (optional)
    echo
    print_color $YELLOW "Enter scope (optional, e.g., auth, database, ui):"
    read -p "Scope: " scope
    
    # Get description
    echo
    print_color $YELLOW "Enter commit description:"
    read -p "Description: " description
    
    # Check for breaking change
    echo
    read -p "Is this a breaking change? (y/N): " breaking
    
    # Build commit message
    if [ -n "$scope" ]; then
        if [[ $breaking == "y" || $breaking == "Y" ]]; then
            commit_msg="${type}!(${scope}): ${description}"
        else
            commit_msg="${type}(${scope}): ${description}"
        fi
    else
        if [[ $breaking == "y" || $breaking == "Y" ]]; then
            commit_msg="${type}!: ${description}"
        else
            commit_msg="${type}: ${description}"
        fi
    fi
    
    # Get body (optional)
    echo
    print_color $YELLOW "Enter commit body (optional, press Enter twice to finish):"
    body=""
    while IFS= read -r line; do
        [ -z "$line" ] && break
        body="${body}${line}\n"
    done
    
    # Add breaking change note if needed
    if [[ $breaking == "y" || $breaking == "Y" ]]; then
        echo
        print_color $YELLOW "Enter breaking change description:"
        read -p "Breaking change: " breaking_desc
        body="${body}\nBREAKING CHANGE: ${breaking_desc}"
    fi
    
    # Show final commit message
    echo
    print_color $BLUE "Final commit message:"
    echo "====================="
    echo "$commit_msg"
    if [ -n "$body" ]; then
        echo
        echo -e "$body"
    fi
    echo "====================="
    echo
    
    # Confirm and commit
    read -p "Create this commit? (Y/n): " confirm
    if [[ $confirm != "n" && $confirm != "N" ]]; then
        if [ -n "$body" ]; then
            git commit -m "$commit_msg" -m "$(echo -e "$body")"
        else
            git commit -m "$commit_msg"
        fi
        print_color $GREEN "✅ Commit created successfully!"
        echo
        print_color $YELLOW "Next steps:"
        echo "1. Push to main: git push origin main"
        echo "2. Release Please will analyze commits and create a release PR"
        echo "3. Review and merge the release PR to trigger builds"
    else
        print_color $YELLOW "Commit cancelled."
    fi
}

show_status() {
    print_header
    print_color $GREEN "Current version: $(get_current_version)"
    echo
    
    print_color $YELLOW "Git status:"
    git status --short
    echo
    
    print_color $YELLOW "Recent commits:"
    git log --oneline -10
    echo
    
    print_color $YELLOW "Uncommitted changes:"
    if [ -z "$(git status --porcelain)" ]; then
        print_color $GREEN "No uncommitted changes"
    else
        git diff --stat
    fi
}

show_changelog() {
    print_header
    if [ -f "CHANGELOG.md" ]; then
        print_color $YELLOW "Recent changelog entries:"
        head -50 CHANGELOG.md
    else
        print_color $RED "CHANGELOG.md not found"
    fi
}

# Main script logic
case "${1:-help}" in
    "commit")
        # Check if we're in a git repository
        if ! git rev-parse --git-dir > /dev/null 2>&1; then
            print_color $RED "Error: Not in a git repository"
            exit 1
        fi
        
        # Check for uncommitted changes
        if [ -z "$(git status --porcelain)" ]; then
            print_color $RED "Error: No changes to commit. Please stage your changes first."
            exit 1
        fi
        
        create_commit
        ;;
    "status")
        show_status
        ;;
    "changelog")
        show_changelog
        ;;
    "help"|*)
        show_help
        ;;
esac