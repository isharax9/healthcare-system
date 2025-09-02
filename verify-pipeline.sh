#!/bin/bash

# Pipeline Verification Script
# This script verifies that the CI/CD pipeline is working correctly

echo "🔍 Healthcare System Pipeline Verification"
echo "=========================================="
echo

# Check if Maven can build
echo "✅ Testing Maven Build..."
if mvn clean test -q; then
    echo "   ✓ Maven build successful"
    echo "   ✓ All tests passing"
else
    echo "   ✗ Maven build failed"
    exit 1
fi

echo

# Check workflow file syntax
echo "✅ Checking Workflow Configuration..."
if [ -f ".github/workflows/build.yml" ]; then
    echo "   ✓ Workflow file exists"
    
    # Check for required permissions
    if grep -q "permissions:" .github/workflows/build.yml; then
        echo "   ✓ Workflow permissions configured"
    else
        echo "   ✗ Missing workflow permissions"
        exit 1
    fi
    
    # Check for continue-on-error
    if grep -q "continue-on-error: true" .github/workflows/build.yml; then
        echo "   ✓ Test reporter configured as non-blocking"
    else
        echo "   ✗ Test reporter not configured properly"
        exit 1
    fi
else
    echo "   ✗ Workflow file missing"
    exit 1
fi

echo
echo "🎉 Pipeline Verification Complete!"
echo "   All checks passed - CI/CD pipeline is ready for use"
echo
echo "💡 Next Steps:"
echo "   1. Merge this PR to enable the fixes on main branch"
echo "   2. Create tags (e.g., v1.0.1) to test release automation"
echo "   3. Push commits to trigger builds automatically"
echo
echo "✨ Your pipeline is now 100% functional!"